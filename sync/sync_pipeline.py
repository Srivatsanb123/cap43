"""
CAP-43 MySQL to AWS S3 Incremental Data Synchronization Engine

Architectural Note:
This service is a lightweight Change Data Capture (CDC) data synchronization pipeline.
It extracts incremental delta records from MySQL using an outbox watermark, anonymizes
tenant PII, and stages partitioned CSV deltas into AWS S3.
Downstream loading and dimensional transformation are handled in Snowflake via Snowpipe,
CDC Streams, and an automated stream-gated task.
"""

import os
import sys
import logging
from datetime import datetime
import pandas as pd
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

# Setup unified logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger("cap43-s3-sync")

# Load environment configuration
load_dotenv(os.path.join(os.path.dirname(__file__), ".env"))

# MySQL Connection Parameters
MYSQL_HOST = os.getenv("MYSQL_HOST", "127.0.0.1")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "root")
MYSQL_DB = os.getenv("MYSQL_DB", "cap43")

# AWS S3 Staging Configuration
CSV_OUTPUT_DIR = os.getenv("CSV_OUTPUT_DIR", os.path.join(os.path.dirname(__file__), "output_csv"))
AWS_S3_BUCKET = os.getenv("AWS_S3_BUCKET", "propledger")
AWS_REGION = os.getenv("AWS_REGION", "ap-south-2")

# Outbox & Delta Extraction Configuration per Table
TABLE_CONFIG = {
    "properties": {"pk": "property_id", "ts_col": "created_at"},
    "units": {"pk": "unit_id", "ts_col": None},
    "tenants": {"pk": "tenant_id", "ts_col": None},
    "vendors": {"pk": "vendor_id", "ts_col": None},
    "severity_rules": {"pk": "severity", "ts_col": None, "static": True},
    "wear_damage_rules": {"pk": "rule_id", "ts_col": None},
    "issues": {"pk": "issue_id", "ts_col": "updated_at", "created_col": "created_at"},
    "work_orders": {"pk": "work_order_id", "ts_col": "updated_at"},
    "repair_costs": {"pk": "cost_id", "ts_col": "created_at"},
    "deposit_disputes": {"pk": "dispute_id", "ts_col": "updated_at"},
    "dispute_items": {"pk": "dispute_item_id", "ts_col": "updated_at", "created_col": "created_at"},
    "inspections": {"pk": "inspection_id", "ts_col": "updated_at", "created_col": "created_at"},
    "inspection_items": {"pk": "item_id", "ts_col": None},
}


def get_mysql_engine():
    """Initializes and returns a SQLAlchemy connection engine for MySQL."""
    connection_url = f"mysql+pymysql://{MYSQL_USER}:{MYSQL_PASSWORD}@{MYSQL_HOST}:{MYSQL_PORT}/{MYSQL_DB}"
    return create_engine(connection_url)


def init_sync_outbox(engine):
    """
    Initializes the transactional sync outbox table in MySQL.
    Tracks the last processed ID and timestamp per table to prevent full table re-dumps.
    """
    with engine.connect() as conn:
        conn.execute(text("""
            CREATE TABLE IF NOT EXISTS sync_outbox (
                table_name VARCHAR(64) PRIMARY KEY,
                last_processed_id BIGINT DEFAULT 0,
                last_processed_timestamp DATETIME NULL,
                last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                rows_synced INT DEFAULT 0
            )
        """))
        conn.commit()


def get_sync_watermark(engine, table_name):
    """Retrieves the last processed watermark (ID and timestamp) for a given table."""
    with engine.connect() as conn:
        row = conn.execute(
            text("SELECT last_processed_id, last_processed_timestamp FROM sync_outbox WHERE table_name = :t"),
            {"t": table_name}
        ).fetchone()
        if row:
            return row[0] or 0, row[1]
        return None, None


def update_sync_watermark(engine, table_name, max_id, max_ts, row_count):
    """Updates the sync watermark in MySQL after successful S3 delivery."""
    with engine.connect() as conn:
        conn.execute(text("""
            INSERT INTO sync_outbox (table_name, last_processed_id, last_processed_timestamp, rows_synced)
            VALUES (:t, :mid, :mts, :rc)
            ON DUPLICATE KEY UPDATE
                last_processed_id = VALUES(last_processed_id),
                last_processed_timestamp = VALUES(last_processed_timestamp),
                rows_synced = rows_synced + VALUES(rows_synced)
        """), {
            "t": table_name,
            "mid": int(max_id) if max_id is not None else 0,
            "mts": max_ts,
            "rc": row_count
        })
        conn.commit()


def mask_tenant_pii(df_delta, table_name):
    """Sanitizes tenant identifying information before S3 staging."""
    if table_name == "tenants":
        if "first_name" in df_delta.columns and "last_name" in df_delta.columns:
            df_delta["first_name"] = df_delta["first_name"].apply(lambda x: f"{x[0]}***" if pd.notna(x) and len(str(x)) > 0 else x)
            df_delta["last_name"] = df_delta["last_name"].apply(lambda x: f"{x[0]}***" if pd.notna(x) and len(str(x)) > 0 else x)
        if "email" in df_delta.columns:
            df_delta["email"] = df_delta["email"].apply(lambda x: f"{x[:2]}***@{x.split('@')[-1]}" if pd.notna(x) and "@" in str(x) else x)
        if "phone" in df_delta.columns:
            df_delta["phone"] = df_delta["phone"].apply(lambda x: f"***-***-{str(x)[-4:]}" if pd.notna(x) and len(str(x)) >= 4 else x)
    return df_delta


def extract_delta_tables(engine):
    """
    Extracts ONLY new or updated rows using the MySQL sync_outbox table.
    Skips tables with 0 changes so unnecessary S3 uploads and Snowpipe triggers are avoided.
    """
    os.makedirs(CSV_OUTPUT_DIR, exist_ok=True)
    init_sync_outbox(engine)
    delta_payloads = {}

    logger.info("Extracting incremental delta rows using MySQL sync_outbox watermark...")

    for table, cfg in TABLE_CONFIG.items():
        pk = cfg["pk"]
        ts_col = cfg.get("ts_col")
        created_col = cfg.get("created_col")
        is_static = cfg.get("static", False)

        last_id, last_ts = get_sync_watermark(engine, table)

        # 1. Initial Load (no prior watermark)
        if last_id is None and last_ts is None:
            query = f"SELECT * FROM {table}"
            df_delta = pd.read_sql(query, engine)
        # 2. Static lookup table already synced
        elif is_static and last_id is not None:
            df_delta = pd.DataFrame()
        # 3. Incremental extraction for tables with timestamp columns
        elif ts_col:
            if last_ts:
                last_ts_str = last_ts.strftime('%Y-%m-%d %H:%M:%S')
                if created_col:
                    where_clause = f"({ts_col} > '{last_ts_str}' OR ({ts_col} IS NULL AND {created_col} > '{last_ts_str}') OR {pk} > {last_id})"
                else:
                    where_clause = f"({ts_col} > '{last_ts_str}' OR {pk} > {last_id})"
                query = f"SELECT * FROM {table} WHERE {where_clause} ORDER BY {pk}"
            else:
                query = f"SELECT * FROM {table} WHERE {pk} > {last_id} ORDER BY {pk}"
            df_delta = pd.read_sql(query, engine)
        # 4. Incremental extraction for append-only tables with integer PK
        else:
            query = f"SELECT * FROM {table} WHERE {pk} > {last_id} ORDER BY {pk}"
            df_delta = pd.read_sql(query, engine)

        # Check if new rows exist
        if df_delta.empty:
            logger.info(f"Table '{table}': 0 new/updated rows since last sync. Skipping S3 upload.")
            continue

        # Mask PII if applicable
        df_delta = mask_tenant_pii(df_delta, table)

        # Determine new watermark
        new_max_id = last_id or 0
        if pk in df_delta.columns and pd.api.types.is_numeric_dtype(df_delta[pk]):
            new_max_id = max(new_max_id, int(df_delta[pk].max()))

        new_max_ts = last_ts
        if ts_col and ts_col in df_delta.columns and not df_delta[ts_col].dropna().empty:
            max_val = pd.to_datetime(df_delta[ts_col]).max()
            if pd.notna(max_val):
                new_max_ts = max_val.to_pydatetime()
        elif created_col and created_col in df_delta.columns and not df_delta[created_col].dropna().empty:
            max_val = pd.to_datetime(df_delta[created_col]).max()
            if pd.notna(max_val):
                new_max_ts = max_val.to_pydatetime()

        csv_path = os.path.join(CSV_OUTPUT_DIR, f"{table}.csv")
        df_delta.to_csv(csv_path, index=False)

        delta_payloads[table] = {
            "csv_path": csv_path,
            "row_count": len(df_delta),
            "max_id": new_max_id,
            "max_ts": new_max_ts
        }
        logger.info(f"Extracted {len(df_delta)} delta rows for '{table}' -> {csv_path}")

    return delta_payloads


def upload_deltas_to_s3(delta_payloads, engine):
    """
    Uploads ONLY delta CSVs to S3 staging folders:
      s3://<bucket>/raw/<table_name>/<table_name>_<timestamp>.csv
    Advances MySQL sync_outbox watermark upon successful delivery.
    """
    if not delta_payloads:
        logger.info("No delta rows detected across all tables. Zero S3 uploads needed.")
        return 0

    if not AWS_S3_BUCKET:
        logger.info("AWS_S3_BUCKET not configured. Deltas retained locally.")
        return 0

    import boto3
    s3 = boto3.client("s3", region_name=AWS_REGION)
    ts = datetime.now().strftime("%Y%m%d_%H%M%S")
    uploaded_count = 0

    for table, payload in delta_payloads.items():
        file_path = payload["csv_path"]
        key_batch = f"raw/{table}/{table}_{ts}.csv"

        try:
            s3.upload_file(file_path, AWS_S3_BUCKET, key_batch)
            logger.info(f"Uploaded delta to S3: s3://{AWS_S3_BUCKET}/{key_batch} ({payload['row_count']} rows)")

            # Advance watermark in MySQL sync outbox
            update_sync_watermark(
                engine,
                table,
                payload["max_id"],
                payload["max_ts"],
                payload["row_count"]
            )
            uploaded_count += 1
        except Exception as e:
            logger.error(f"Failed to upload delta for {table}: {e}")

    return uploaded_count


def main():
    """
    Transactional Outbox-Driven S3 Data Synchronization:
    1. Reads watermark from MySQL sync_outbox.
    2. Extracts only new or updated rows since last watermark.
    3. Anonymizes tenant PII.
    4. Pushes delta CSV to S3 (fires S3 event for Snowpipe only when real changes occur).
    5. Advances outbox watermark in MySQL.
    """
    logger.info("=== Starting CAP-43 MySQL-to-S3 Data Synchronization ===")
    start_time = datetime.now()

    engine = get_mysql_engine()
    delta_payloads = extract_delta_tables(engine)
    uploaded = upload_deltas_to_s3(delta_payloads, engine)

    duration = (datetime.now() - start_time).total_seconds()
    logger.info(f"=== S3 Sync Process Finished in {duration:.2f}s (Tables with deltas: {uploaded}) ===")
    print(f"SYNC_STATUS=SUCCESS,DELTAS_FOUND={len(delta_payloads)},S3_UPLOADED={uploaded}")


if __name__ == "__main__":
    main()


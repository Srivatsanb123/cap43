USE cap43;

-- load schema and seed data
SOURCE /docker-entrypoint-initdb.d/scripts/01_schema.sql;
SOURCE /docker-entrypoint-initdb.d/scripts/02_seed_reference.sql;
SOURCE /docker-entrypoint-initdb.d/scripts/03_seed_demo.sql;

-- outbox table to track sync watermarks
CREATE TABLE IF NOT EXISTS sync_outbox (
    table_name VARCHAR(64) PRIMARY KEY,
    last_processed_id BIGINT DEFAULT 0,
    last_processed_timestamp DATETIME NULL,
    last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    rows_synced INT DEFAULT 0
);

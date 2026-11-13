-- setup warehouse
USE ROLE ACCOUNTADMIN;

CREATE WAREHOUSE IF NOT EXISTS CAP43_WH
  WITH WAREHOUSE_SIZE = 'XSMALL'
  AUTO_SUSPEND = 60
  AUTO_RESUME = TRUE
  INITIALLY_SUSPENDED = TRUE;

USE WAREHOUSE CAP43_WH;

-- database and schemas
CREATE DATABASE IF NOT EXISTS CAP43_DB;
USE DATABASE CAP43_DB;

CREATE SCHEMA IF NOT EXISTS RAW;
CREATE SCHEMA IF NOT EXISTS ANALYTICS;

-- s3 storage integration (replace role arn and bucket before running)
CREATE OR REPLACE STORAGE INTEGRATION S3_CAP43_INTEGRATION
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = 'S3'
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::<ACCOUNT_ID>:role/<ROLE_NAME>'
  STORAGE_ALLOWED_LOCATIONS = ('s3://propledger/raw/');

-- get iam user arn and external id for the aws role trust policy
DESC INTEGRATION S3_CAP43_INTEGRATION;

-- csv file format
USE SCHEMA RAW;

CREATE OR REPLACE FILE FORMAT CAP43_DB.RAW.CSV_STAGE_FORMAT
  TYPE = 'CSV'
  FIELD_DELIMITER = ','
  SKIP_HEADER = 1
  FIELD_OPTIONALLY_ENCLOSED_BY = '"'
  NULL_IF = ('NULL', 'None', '', 'nan', 'NaN')
  EMPTY_FIELD_AS_NULL = TRUE
  TIMESTAMP_FORMAT = 'AUTO';

-- external stage pointing to s3 raw folder
CREATE OR REPLACE STAGE CAP43_DB.RAW.S3_RAW_STAGE
  STORAGE_INTEGRATION = S3_CAP43_INTEGRATION
  URL = 's3://propledger/raw/'
  FILE_FORMAT = CAP43_DB.RAW.CSV_STAGE_FORMAT;

-- raw staging tables
CREATE TABLE IF NOT EXISTS RAW_PROPERTIES (
    property_id INT,
    name VARCHAR(100),
    address_line VARCHAR(200),
    city VARCHAR(60),
    state VARCHAR(60),
    postal_code VARCHAR(12),
    created_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_UNITS (
    unit_id INT,
    property_id INT,
    unit_number VARCHAR(20),
    floor_no INT,
    bedrooms INT,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_TENANTS (
    tenant_id INT,
    user_id INT,
    tenant_ref VARCHAR(12),
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_VENDORS (
    vendor_id INT,
    user_id INT,
    company_name VARCHAR(100),
    trade VARCHAR(40),
    active BOOLEAN,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_SEVERITY_RULES (
    severity VARCHAR(20),
    rank_order INT,
    sla_hours INT,
    escalate_l1_pct INT,
    escalate_l2_pct INT,
    description VARCHAR(255),
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_WEAR_DAMAGE_RULES (
    rule_id INT,
    rule_name VARCHAR(120),
    item_category VARCHAR(30),
    min_age_years INT,
    max_age_years INT,
    move_in_condition VARCHAR(20),
    move_out_condition VARCHAR(20),
    cause VARCHAR(30),
    classification VARCHAR(20),
    priority INT,
    active BOOLEAN,
    notes VARCHAR(255),
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_ISSUES (
    issue_id INT,
    unit_id INT,
    tenant_id INT,
    category VARCHAR(30),
    title VARCHAR(120),
    description VARCHAR(2000),
    severity VARCHAR(20),
    severity_source VARCHAR(10),
    status VARCHAR(20),
    escalation_level INT,
    created_at TIMESTAMP,
    acknowledged_at TIMESTAMP,
    sla_due_at TIMESTAMP,
    resolved_at TIMESTAMP,
    updated_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_WORK_ORDERS (
    work_order_id INT,
    issue_id INT,
    inspection_item_id INT,
    vendor_id INT,
    status VARCHAR(20),
    assigned_at TIMESTAMP,
    assigned_by_user_id INT,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    vendor_notes VARCHAR(1000),
    updated_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_REPAIR_COSTS (
    cost_id INT,
    issue_id INT,
    work_order_id INT,
    inspection_item_id INT,
    vendor_id INT,
    entry_type VARCHAR(10),
    amount DECIMAL(10,2),
    cost_type VARCHAR(20),
    description VARCHAR(200),
    invoice_ref VARCHAR(50),
    reverses_cost_id INT,
    incurred_at TIMESTAMP,
    entered_by_user_id INT,
    created_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_DEPOSIT_DISPUTES (
    dispute_id INT,
    lease_id INT,
    tenant_id INT,
    deposit_held DECIMAL(10,2),
    status VARCHAR(30),
    opened_at TIMESTAMP,
    resolved_at TIMESTAMP,
    updated_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_DISPUTE_ITEMS (
    dispute_item_id INT,
    dispute_id INT,
    move_out_item_id INT,
    move_in_item_id INT,
    claimed_amount DECIMAL(10,2),
    tenant_position VARCHAR(20),
    tenant_comment VARCHAR(1000),
    system_classification VARCHAR(30),
    applied_rule_id INT,
    rule_trace VARCHAR(2000),
    operator_decision VARCHAR(30),
    decided_amount DECIMAL(10,2),
    decided_by_user_id INT,
    decided_at TIMESTAMP,
    decision_notes VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_INSPECTIONS (
    inspection_id INT,
    lease_id INT,
    inspection_type VARCHAR(10),
    inspected_on DATE,
    inspector_user_id INT,
    notes VARCHAR(2000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS RAW_INSPECTION_ITEMS (
    item_id INT,
    inspection_id INT,
    item_category VARCHAR(30),
    item_name VARCHAR(80),
    location VARCHAR(60),
    condition_rating VARCHAR(10),
    item_age_years INT,
    cause_noted VARCHAR(15),
    remarks VARCHAR(1000),
    source_file VARCHAR(500),
    ingested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

-- snowpipes for auto-ingest
CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_PROPERTIES
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_PROPERTIES
(
    property_id, name, address_line, city, state, postal_code, created_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::VARCHAR, $3::VARCHAR, $4::VARCHAR, $5::VARCHAR, $6::VARCHAR, $7::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/properties/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_UNITS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_UNITS
(
    unit_id, property_id, unit_number, floor_no, bedrooms,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::VARCHAR, $4::INT, $5::INT,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/units/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_TENANTS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_TENANTS
(
    tenant_id, user_id, tenant_ref,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::VARCHAR,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/tenants/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_VENDORS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_VENDORS
(
    vendor_id, user_id, company_name, trade, active,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::VARCHAR, $4::VARCHAR, $5::BOOLEAN,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/vendors/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_SEVERITY_RULES
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_SEVERITY_RULES
(
    severity, rank_order, sla_hours, escalate_l1_pct, escalate_l2_pct, description,
    source_file, ingested_at
)
FROM (
    SELECT $1::VARCHAR, $2::INT, $3::INT, $4::INT, $5::INT, $6::VARCHAR,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/severity_rules/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_WEAR_DAMAGE_RULES
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_WEAR_DAMAGE_RULES
(
    rule_id, rule_name, item_category, min_age_years, max_age_years,
    move_in_condition, move_out_condition, cause, classification, priority,
    active, notes,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::VARCHAR, $3::VARCHAR, $4::INT, $5::INT,
           $6::VARCHAR, $7::VARCHAR, $8::VARCHAR, $9::VARCHAR, $10::INT,
           $11::BOOLEAN, $12::VARCHAR,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/wear_damage_rules/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_ISSUES
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_ISSUES
(
    issue_id, unit_id, tenant_id, category, title, description,
    severity, severity_source, status, escalation_level,
    created_at, acknowledged_at, sla_due_at, resolved_at, updated_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::INT, $4::VARCHAR, $5::VARCHAR, $6::VARCHAR,
           $7::VARCHAR, $8::VARCHAR, $9::VARCHAR, $10::INT,
           $11::TIMESTAMP, $12::TIMESTAMP, $13::TIMESTAMP, $14::TIMESTAMP, $15::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/issues/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_WORK_ORDERS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_WORK_ORDERS
(
    work_order_id, issue_id, inspection_item_id, vendor_id, status,
    assigned_at, assigned_by_user_id, accepted_at, started_at, completed_at, vendor_notes, updated_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::INT, $4::INT, $5::VARCHAR,
           $6::TIMESTAMP, $7::INT, $8::TIMESTAMP, $9::TIMESTAMP, $10::TIMESTAMP, $11::VARCHAR, $12::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/work_orders/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_REPAIR_COSTS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_REPAIR_COSTS
(
    cost_id, issue_id, work_order_id, inspection_item_id, vendor_id,
    entry_type, amount, cost_type, description, invoice_ref, reverses_cost_id,
    incurred_at, entered_by_user_id, created_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::INT, $4::INT, $5::INT,
           $6::VARCHAR, $7::DECIMAL(10,2), $8::VARCHAR, $9::VARCHAR, $10::VARCHAR, $11::INT,
           $12::TIMESTAMP, $13::INT, $14::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/repair_costs/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_DEPOSIT_DISPUTES
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_DEPOSIT_DISPUTES
(
    dispute_id, lease_id, tenant_id, deposit_held, status,
    opened_at, resolved_at, updated_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::INT, $4::DECIMAL(10,2), $5::VARCHAR,
           $6::TIMESTAMP, $7::TIMESTAMP, $8::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/deposit_disputes/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_DISPUTE_ITEMS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_DISPUTE_ITEMS
(
    dispute_item_id, dispute_id, move_out_item_id, move_in_item_id, claimed_amount,
    tenant_position, tenant_comment, system_classification, applied_rule_id, rule_trace,
    operator_decision, decided_amount, decided_by_user_id, decided_at, decision_notes,
    created_at, updated_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::INT, $4::INT, $5::DECIMAL(10,2),
           $6::VARCHAR, $7::VARCHAR, $8::VARCHAR, $9::INT, $10::VARCHAR,
           $11::VARCHAR, $12::DECIMAL(10,2), $13::INT, $14::TIMESTAMP, $15::VARCHAR,
           $16::TIMESTAMP, $17::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/dispute_items/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_INSPECTIONS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_INSPECTIONS
(
    inspection_id, lease_id, inspection_type, inspected_on, inspector_user_id, notes,
    created_at, updated_at,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::VARCHAR, $4::DATE, $5::INT, $6::VARCHAR,
           $7::TIMESTAMP, $8::TIMESTAMP,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/inspections/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

CREATE OR REPLACE PIPE CAP43_DB.RAW.PIPE_INSPECTION_ITEMS
    AUTO_INGEST = TRUE
AS
COPY INTO CAP43_DB.RAW.RAW_INSPECTION_ITEMS
(
    item_id, inspection_id, item_category, item_name, location,
    condition_rating, item_age_years, cause_noted, remarks,
    source_file, ingested_at
)
FROM (
    SELECT $1::INT, $2::INT, $3::VARCHAR, $4::VARCHAR, $5::VARCHAR,
           $6::VARCHAR, $7::INT, $8::VARCHAR, $9::VARCHAR,
           METADATA$FILENAME, CURRENT_TIMESTAMP()
    FROM @CAP43_DB.RAW.S3_RAW_STAGE/inspection_items/
)
FILE_FORMAT = (FORMAT_NAME = CAP43_DB.RAW.CSV_STAGE_FORMAT);

-- verify pipe status and get sqs queue arn
DESC PIPE CAP43_DB.RAW.PIPE_ISSUES;

SELECT SYSTEM$PIPE_STATUS('CAP43_DB.RAW.PIPE_ISSUES');

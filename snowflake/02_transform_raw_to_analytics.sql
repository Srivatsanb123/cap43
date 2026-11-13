-- analytics schema setup and transformations

USE ROLE ACCOUNTADMIN;
USE DATABASE CAP43_DB;
USE SCHEMA ANALYTICS;
USE WAREHOUSE CAP43_WH;

-- dimension tables

CREATE TABLE IF NOT EXISTS DIM_PROPERTY (
    property_id INT PRIMARY KEY,
    name VARCHAR(100),
    city VARCHAR(60),
    state VARCHAR(60),
    postal_code VARCHAR(12)
);

CREATE TABLE IF NOT EXISTS DIM_UNIT (
    unit_id INT PRIMARY KEY,
    property_id INT,
    unit_number VARCHAR(20),
    floor_no INT,
    bedrooms INT
);

CREATE TABLE IF NOT EXISTS DIM_TENANT (
    tenant_id INT PRIMARY KEY,
    tenant_ref VARCHAR(12)
);

CREATE TABLE IF NOT EXISTS DIM_VENDOR (
    vendor_id INT PRIMARY KEY,
    company_name VARCHAR(100),
    trade VARCHAR(40),
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS DIM_SEVERITY_RULE (
    severity VARCHAR(20) PRIMARY KEY,
    rank_order INT,
    sla_hours INT,
    escalate_l1_pct INT,
    escalate_l2_pct INT,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS DIM_WEAR_DAMAGE_RULE (
    rule_id INT PRIMARY KEY,
    rule_name VARCHAR(120),
    item_category VARCHAR(30),
    classification VARCHAR(20),
    priority INT
);

-- fact tables

CREATE TABLE IF NOT EXISTS FACT_MAINTENANCE_ISSUES (
    issue_id INT PRIMARY KEY,
    unit_id INT,
    tenant_id INT,
    category VARCHAR(30),
    severity VARCHAR(20),
    status VARCHAR(20),
    escalation_level INT,
    created_at TIMESTAMP,
    acknowledged_at TIMESTAMP,
    sla_due_at TIMESTAMP,
    resolved_at TIMESTAMP,
    sla_hours INT,
    response_hours FLOAT,
    resolution_hours FLOAT,
    is_sla_breached BOOLEAN,
    is_overdue BOOLEAN
);

CREATE TABLE IF NOT EXISTS FACT_REPAIR_COSTS (
    cost_id INT PRIMARY KEY,
    issue_id INT,
    work_order_id INT,
    vendor_id INT,
    entry_type VARCHAR(10),
    amount DECIMAL(10,2),
    cost_type VARCHAR(20),
    incurred_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS FACT_DEPOSIT_DISPUTES (
    dispute_id INT,
    dispute_item_id INT PRIMARY KEY,
    lease_id INT,
    tenant_id INT,
    item_category VARCHAR(30),
    claimed_amount DECIMAL(10,2),
    tenant_position VARCHAR(20),
    system_classification VARCHAR(30),
    operator_decision VARCHAR(30),
    decided_amount DECIMAL(10,2),
    amount_saved_by_tenant DECIMAL(10,2),
    routed_to_human BOOLEAN,
    opened_at TIMESTAMP,
    resolved_at TIMESTAMP
);

-- cdc streams on raw tables
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_PROPERTIES ON TABLE CAP43_DB.RAW.RAW_PROPERTIES;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_UNITS ON TABLE CAP43_DB.RAW.RAW_UNITS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_TENANTS ON TABLE CAP43_DB.RAW.RAW_TENANTS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_VENDORS ON TABLE CAP43_DB.RAW.RAW_VENDORS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_SEVERITY_RULES ON TABLE CAP43_DB.RAW.RAW_SEVERITY_RULES;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_WEAR_DAMAGE_RULES ON TABLE CAP43_DB.RAW.RAW_WEAR_DAMAGE_RULES;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_ISSUES ON TABLE CAP43_DB.RAW.RAW_ISSUES;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_WORK_ORDERS ON TABLE CAP43_DB.RAW.RAW_WORK_ORDERS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_REPAIR_COSTS ON TABLE CAP43_DB.RAW.RAW_REPAIR_COSTS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_DEPOSIT_DISPUTES ON TABLE CAP43_DB.RAW.RAW_DEPOSIT_DISPUTES;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_DISPUTE_ITEMS ON TABLE CAP43_DB.RAW.RAW_DISPUTE_ITEMS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_INSPECTIONS ON TABLE CAP43_DB.RAW.RAW_INSPECTIONS;
CREATE OR REPLACE STREAM CAP43_DB.RAW.STREAM_RAW_INSPECTION_ITEMS ON TABLE CAP43_DB.RAW.RAW_INSPECTION_ITEMS;

-- stored procedure to transform raw delta into analytics
CREATE OR REPLACE PROCEDURE SP_TRANSFORM_RAW_TO_ANALYTICS()
RETURNS STRING
LANGUAGE SQL
EXECUTE AS CALLER
AS
$$
BEGIN
    BEGIN TRANSACTION;

    CREATE OR REPLACE TEMPORARY TABLE STG_PROPERTIES AS
    SELECT property_id, name, city, state, postal_code, METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_PROPERTIES
    QUALIFY ROW_NUMBER() OVER (PARTITION BY property_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_UNITS AS
    SELECT unit_id, property_id, unit_number, floor_no, bedrooms, METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_UNITS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY unit_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_TENANTS AS
    SELECT tenant_id, tenant_ref, METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_TENANTS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY tenant_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_VENDORS AS
    SELECT vendor_id, company_name, trade, active, METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_VENDORS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY vendor_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_SEVERITY_RULES AS
    SELECT severity, rank_order, sla_hours, escalate_l1_pct, escalate_l2_pct, description, METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_SEVERITY_RULES
    QUALIFY ROW_NUMBER() OVER (PARTITION BY severity ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_WEAR_DAMAGE_RULES AS
    SELECT rule_id, rule_name, item_category, classification, priority, active, METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_WEAR_DAMAGE_RULES
    QUALIFY ROW_NUMBER() OVER (PARTITION BY rule_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_ISSUES AS
    SELECT issue_id, unit_id, tenant_id, category, severity, status,
           escalation_level, created_at, acknowledged_at, sla_due_at, resolved_at,
           METADATA$ACTION, METADATA$ISUPDATE
    FROM CAP43_DB.RAW.STREAM_RAW_ISSUES
    QUALIFY ROW_NUMBER() OVER (PARTITION BY issue_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_WORK_ORDERS AS
    SELECT work_order_id, issue_id, inspection_item_id, vendor_id, status, updated_at
    FROM CAP43_DB.RAW.STREAM_RAW_WORK_ORDERS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY work_order_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_REPAIR_COSTS AS
    SELECT cost_id, issue_id, work_order_id, vendor_id, entry_type, amount, cost_type, incurred_at
    FROM CAP43_DB.RAW.STREAM_RAW_REPAIR_COSTS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY cost_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_DEPOSIT_DISPUTES AS
    SELECT dispute_id, lease_id, tenant_id, deposit_held, status, opened_at, resolved_at
    FROM CAP43_DB.RAW.STREAM_RAW_DEPOSIT_DISPUTES
    QUALIFY ROW_NUMBER() OVER (PARTITION BY dispute_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_DISPUTE_ITEMS AS
    SELECT dispute_item_id, dispute_id, move_out_item_id, claimed_amount, tenant_position,
           system_classification, operator_decision, decided_amount
    FROM CAP43_DB.RAW.STREAM_RAW_DISPUTE_ITEMS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY dispute_item_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_INSPECTIONS AS
    SELECT inspection_id, lease_id, inspection_type, inspected_on
    FROM CAP43_DB.RAW.STREAM_RAW_INSPECTIONS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY inspection_id ORDER BY ingested_at DESC) = 1;

    CREATE OR REPLACE TEMPORARY TABLE STG_INSPECTION_ITEMS AS
    SELECT item_id, inspection_id, item_category, item_name, location
    FROM CAP43_DB.RAW.STREAM_RAW_INSPECTION_ITEMS
    QUALIFY ROW_NUMBER() OVER (PARTITION BY item_id ORDER BY ingested_at DESC) = 1;

    -- merge dim_property
    MERGE INTO CAP43_DB.ANALYTICS.DIM_PROPERTY tgt
    USING STG_PROPERTIES src
    ON tgt.property_id = src.property_id
    WHEN MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        UPDATE SET tgt.name = src.name, tgt.city = src.city, tgt.state = src.state, tgt.postal_code = src.postal_code
    WHEN NOT MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        INSERT (property_id, name, city, state, postal_code)
        VALUES (src.property_id, src.name, src.city, src.state, src.postal_code);

    -- merge dim_unit
    MERGE INTO CAP43_DB.ANALYTICS.DIM_UNIT tgt
    USING STG_UNITS src
    ON tgt.unit_id = src.unit_id
    WHEN MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        UPDATE SET tgt.property_id = src.property_id, tgt.unit_number = src.unit_number,
                   tgt.floor_no = src.floor_no, tgt.bedrooms = src.bedrooms
    WHEN NOT MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        INSERT (unit_id, property_id, unit_number, floor_no, bedrooms)
        VALUES (src.unit_id, src.property_id, src.unit_number, src.floor_no, src.bedrooms);

    -- merge dim_tenant
    MERGE INTO CAP43_DB.ANALYTICS.DIM_TENANT tgt
    USING STG_TENANTS src
    ON tgt.tenant_id = src.tenant_id
    WHEN MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        UPDATE SET tgt.tenant_ref = src.tenant_ref
    WHEN NOT MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        INSERT (tenant_id, tenant_ref)
        VALUES (src.tenant_id, src.tenant_ref);

    -- merge dim_vendor
    MERGE INTO CAP43_DB.ANALYTICS.DIM_VENDOR tgt
    USING STG_VENDORS src
    ON tgt.vendor_id = src.vendor_id
    WHEN MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        UPDATE SET tgt.company_name = src.company_name, tgt.trade = src.trade, tgt.active = src.active
    WHEN NOT MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        INSERT (vendor_id, company_name, trade, active)
        VALUES (src.vendor_id, src.company_name, src.trade, src.active);

    -- merge dim_severity_rule
    MERGE INTO CAP43_DB.ANALYTICS.DIM_SEVERITY_RULE tgt
    USING STG_SEVERITY_RULES src
    ON tgt.severity = src.severity
    WHEN MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        UPDATE SET tgt.rank_order = src.rank_order, tgt.sla_hours = src.sla_hours,
                   tgt.escalate_l1_pct = src.escalate_l1_pct, tgt.escalate_l2_pct = src.escalate_l2_pct,
                   tgt.description = src.description
    WHEN NOT MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        INSERT (severity, rank_order, sla_hours, escalate_l1_pct, escalate_l2_pct, description)
        VALUES (src.severity, src.rank_order, src.sla_hours, src.escalate_l1_pct, src.escalate_l2_pct, src.description);

    -- merge dim_wear_damage_rule
    MERGE INTO CAP43_DB.ANALYTICS.DIM_WEAR_DAMAGE_RULE tgt
    USING STG_WEAR_DAMAGE_RULES src
    ON tgt.rule_id = src.rule_id
    WHEN MATCHED AND src.METADATA$ACTION = 'INSERT' THEN
        UPDATE SET tgt.rule_name = src.rule_name, tgt.item_category = src.item_category,
                   tgt.classification = src.classification, tgt.priority = src.priority
    WHEN NOT MATCHED AND src.METADATA$ACTION = 'INSERT' AND src.active = TRUE THEN
        INSERT (rule_id, rule_name, item_category, classification, priority)
        VALUES (src.rule_id, src.rule_name, src.item_category, src.classification, src.priority);

    -- merge fact_maintenance_issues
    MERGE INTO CAP43_DB.ANALYTICS.FACT_MAINTENANCE_ISSUES tgt
    USING (
        SELECT i.issue_id, i.unit_id, i.tenant_id, i.category, i.severity, i.status,
               i.escalation_level, i.created_at, i.acknowledged_at, i.sla_due_at, i.resolved_at,
               COALESCE(r.sla_hours, 48) AS sla_hours,
               ROUND(TIMEDIFF('second', i.created_at, i.acknowledged_at) / 3600.0, 2) AS response_hours,
               ROUND(TIMEDIFF('second', i.created_at, i.resolved_at) / 3600.0, 2) AS resolution_hours,
               CASE WHEN i.resolved_at IS NOT NULL AND i.resolved_at > i.sla_due_at THEN TRUE ELSE FALSE END AS is_sla_breached
        FROM STG_ISSUES i
        LEFT JOIN CAP43_DB.RAW.RAW_SEVERITY_RULES r ON i.severity = r.severity
        WHERE i.METADATA$ACTION = 'INSERT'
    ) src
    ON tgt.issue_id = src.issue_id
    WHEN MATCHED THEN
        UPDATE SET tgt.unit_id = src.unit_id, tgt.tenant_id = src.tenant_id, tgt.category = src.category,
                   tgt.severity = src.severity, tgt.status = src.status, tgt.escalation_level = src.escalation_level,
                   tgt.acknowledged_at = src.acknowledged_at, tgt.sla_due_at = src.sla_due_at,
                   tgt.resolved_at = src.resolved_at, tgt.sla_hours = src.sla_hours,
                   tgt.response_hours = src.response_hours, tgt.resolution_hours = src.resolution_hours,
                   tgt.is_sla_breached = src.is_sla_breached
    WHEN NOT MATCHED THEN
        INSERT (issue_id, unit_id, tenant_id, category, severity, status, escalation_level,
                created_at, acknowledged_at, sla_due_at, resolved_at, sla_hours, response_hours,
                resolution_hours, is_sla_breached, is_overdue)
        VALUES (src.issue_id, src.unit_id, src.tenant_id, src.category, src.severity, src.status,
                src.escalation_level, src.created_at, src.acknowledged_at, src.sla_due_at, src.resolved_at,
                src.sla_hours, src.response_hours, src.resolution_hours, src.is_sla_breached, FALSE);

    -- merge fact_repair_costs
    MERGE INTO CAP43_DB.ANALYTICS.FACT_REPAIR_COSTS tgt
    USING STG_REPAIR_COSTS src
    ON tgt.cost_id = src.cost_id
    WHEN MATCHED THEN
        UPDATE SET tgt.issue_id = src.issue_id, tgt.work_order_id = src.work_order_id,
                   tgt.vendor_id = src.vendor_id, tgt.entry_type = src.entry_type,
                   tgt.amount = src.amount, tgt.cost_type = src.cost_type, tgt.incurred_at = src.incurred_at
    WHEN NOT MATCHED THEN
        INSERT (cost_id, issue_id, work_order_id, vendor_id, entry_type, amount, cost_type, incurred_at)
        VALUES (src.cost_id, src.issue_id, src.work_order_id, src.vendor_id, src.entry_type, src.amount, src.cost_type, src.incurred_at);

    -- merge fact_deposit_disputes
    MERGE INTO CAP43_DB.ANALYTICS.FACT_DEPOSIT_DISPUTES tgt
    USING (
        SELECT d.dispute_id, di.dispute_item_id, d.lease_id, d.tenant_id,
               COALESCE(ii.item_category, 'General') AS item_category,
               di.claimed_amount, di.tenant_position, di.system_classification, di.operator_decision,
               di.decided_amount,
               GREATEST(0, di.claimed_amount - COALESCE(di.decided_amount, di.claimed_amount)) AS amount_saved_by_tenant,
               CASE WHEN di.operator_decision IS NOT NULL AND di.operator_decision != di.system_classification THEN TRUE ELSE FALSE END AS routed_to_human,
               d.opened_at, d.resolved_at
        FROM (
            SELECT di.* FROM CAP43_DB.RAW.RAW_DISPUTE_ITEMS di
            WHERE di.dispute_item_id IN (SELECT dispute_item_id FROM STG_DISPUTE_ITEMS)
               OR di.dispute_id IN (SELECT dispute_id FROM STG_DEPOSIT_DISPUTES)
        ) di
        INNER JOIN CAP43_DB.RAW.RAW_DEPOSIT_DISPUTES d ON di.dispute_id = d.dispute_id
        LEFT JOIN CAP43_DB.RAW.RAW_INSPECTION_ITEMS ii ON di.move_out_item_id = ii.item_id
    ) src
    ON tgt.dispute_item_id = src.dispute_item_id
    WHEN MATCHED THEN
        UPDATE SET tgt.item_category = src.item_category, tgt.claimed_amount = src.claimed_amount,
                   tgt.tenant_position = src.tenant_position, tgt.system_classification = src.system_classification,
                   tgt.operator_decision = src.operator_decision, tgt.decided_amount = src.decided_amount,
                   tgt.amount_saved_by_tenant = src.amount_saved_by_tenant, tgt.routed_to_human = src.routed_to_human,
                   tgt.resolved_at = src.resolved_at
    WHEN NOT MATCHED THEN
        INSERT (dispute_id, dispute_item_id, lease_id, tenant_id, item_category, claimed_amount,
                tenant_position, system_classification, operator_decision, decided_amount,
                amount_saved_by_tenant, routed_to_human, opened_at, resolved_at)
        VALUES (src.dispute_id, src.dispute_item_id, src.lease_id, src.tenant_id, src.item_category,
                src.claimed_amount, src.tenant_position, src.system_classification, src.operator_decision,
                src.decided_amount, src.amount_saved_by_tenant, src.routed_to_human, src.opened_at, src.resolved_at);

    COMMIT;
    RETURN 'SUCCESS';

EXCEPTION
    WHEN OTHER THEN
        ROLLBACK;
        RAISE;
END;
$$;

-- task to trigger transform on new stream data
CREATE OR REPLACE TASK TASK_AUTO_TRANSFORM_ANALYTICS
  WAREHOUSE = CAP43_WH
  SCHEDULE = '1 MINUTE'
  WHEN
    SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_PROPERTIES')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_UNITS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_TENANTS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_VENDORS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_SEVERITY_RULES')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_WEAR_DAMAGE_RULES')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_ISSUES')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_WORK_ORDERS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_REPAIR_COSTS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_DEPOSIT_DISPUTES')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_DISPUTE_ITEMS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_INSPECTIONS')
    OR SYSTEM$STREAM_HAS_DATA('CAP43_DB.RAW.STREAM_RAW_INSPECTION_ITEMS')
AS
  CALL SP_TRANSFORM_RAW_TO_ANALYTICS();

ALTER TASK TASK_AUTO_TRANSFORM_ANALYTICS RESUME;

-- reporting views for power bi
CREATE OR REPLACE VIEW V_SLA_COMPLIANCE_SUMMARY AS
SELECT
    severity,
    category,
    COUNT(*) AS total_issues,
    SUM(CASE WHEN is_sla_breached THEN 1 ELSE 0 END) AS breached_count,
    SUM(CASE WHEN status != 'RESOLVED' AND CURRENT_TIMESTAMP() > sla_due_at THEN 1 ELSE 0 END) AS live_overdue_count,
    ROUND(100.0 * (1 - (SUM(CASE WHEN is_sla_breached THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0))), 2) AS sla_compliance_pct,
    ROUND(AVG(resolution_hours), 2) AS avg_resolution_hours,
    ROUND(AVG(response_hours), 2) AS avg_response_hours
FROM FACT_MAINTENANCE_ISSUES
GROUP BY severity, category;

CREATE OR REPLACE VIEW V_DEPOSIT_DISPUTE_METRICS AS
SELECT
    item_category,
    COUNT(dispute_item_id) AS total_dispute_items,
    ROUND(SUM(claimed_amount), 2) AS total_claimed,
    ROUND(SUM(decided_amount), 2) AS total_awarded,
    ROUND(SUM(amount_saved_by_tenant), 2) AS total_saved_by_tenant,
    SUM(CASE WHEN routed_to_human THEN 1 ELSE 0 END) AS human_review_count,
    ROUND(100.0 * SUM(CASE WHEN routed_to_human THEN 1 ELSE 0 END) / NULLIF(COUNT(dispute_item_id), 0), 2) AS human_review_pct
FROM FACT_DEPOSIT_DISPUTES
GROUP BY item_category;

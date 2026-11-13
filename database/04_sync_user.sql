CREATE USER IF NOT EXISTS 'sync_ro'@'localhost' IDENTIFIED BY 'ChangeMe123!';
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'sync_ro'@'localhost';

-- Deliberately NOT granted: users (PII) and audit_log
GRANT SELECT ON cap43.properties          TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.units               TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.tenants             TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.leases              TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.vendors             TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.severity_rules      TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.issue_keywords      TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.wear_damage_rules   TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.system_config       TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.inspections         TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.inspection_items    TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.issues              TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.work_orders         TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.work_order_updates  TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.issue_status_history TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.escalations         TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.repair_costs        TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.deposit_disputes    TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.dispute_items       TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.evidence_files      TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.review_queue        TO 'sync_ro'@'localhost';
GRANT SELECT ON cap43.dispute_events      TO 'sync_ro'@'localhost';
FLUSH PRIVILEGES;


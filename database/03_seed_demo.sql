USE cap43;
SET @pending = 'PENDING_SET_BY_APP';

-- users
INSERT INTO users (user_id, email, password_hash, role, full_name, phone) VALUES
 (1,'admin@cap43.test',        @pending,'ADMIN', 'Operations Admin',   '9000000000'),
 (2,'priya.raman@cap43.test',  @pending,'TENANT','Priya Raman',        '9000000001'),
 (3,'arun.kumar@cap43.test',   @pending,'TENANT','Arun Kumar',         '9000000002'),
 (4,'meena.iyer@cap43.test',   @pending,'TENANT','Meena Iyer',         '9000000003'),
 (5,'karthik.s@cap43.test',    @pending,'TENANT','Karthik Subramanian','9000000004'),
 (6,'fixit@cap43.test',        @pending,'VENDOR','FixIt Plumbing',     '9000000011'),
 (7,'brightspark@cap43.test',  @pending,'VENDOR','BrightSpark',        '9000000012'),
 (8,'handyco@cap43.test',      @pending,'VENDOR','HandyCo',            '9000000013');

-- properties, units, tenants, leases, and vendors
INSERT INTO properties (property_id, name, address_line, city, state, postal_code) VALUES
 (1,'Maple Heights',  '12 Park Avenue, Anna Nagar', 'Chennai','Tamil Nadu','600040'),
 (2,'Lakeview Towers','48 Lake Road, Adyar',        'Chennai','Tamil Nadu','600020');

INSERT INTO units (unit_id, property_id, unit_number, floor_no, bedrooms) VALUES
 (1,1,'A101',1,2),(2,1,'A102',1,2),(3,2,'B201',2,3),(4,2,'B202',2,2);

INSERT INTO tenants (tenant_id, user_id, tenant_ref) VALUES
 (1,2,'TN-0001'),(2,3,'TN-0002'),(3,4,'TN-0003'),(4,5,'TN-0004');

INSERT INTO leases (lease_id, unit_id, tenant_id, start_date, end_date, monthly_rent, deposit_amount, status, move_out_date) VALUES
 (1,1,1,'2025-01-01','2027-12-31',25000,50000,'ACTIVE',NULL),
 (2,2,2,'2025-03-01', CURDATE() - INTERVAL 10 DAY, 20000,40000,'ENDED', CURDATE() - INTERVAL 10 DAY),
 (3,3,3,'2024-06-01', CURDATE() - INTERVAL 26 DAY, 30000,60000,'ENDED', CURDATE() - INTERVAL 26 DAY),
 (4,4,4,'2025-02-01','2027-01-31',22000,45000,'ACTIVE',NULL);

INSERT INTO vendors (vendor_id, user_id, company_name, trade) VALUES
 (1,6,'FixIt Plumbing','PLUMBING'),(2,7,'BrightSpark','ELECTRICAL'),(3,8,'HandyCo','GENERAL');

-- inspections
INSERT INTO inspections (inspection_id, lease_id, inspection_type, inspected_on, inspector_user_id, notes) VALUES
 (1,2,'MOVE_IN',  '2025-03-01', 1, 'Unit handed over in good order'),
 (2,2,'MOVE_OUT', CURDATE() - INTERVAL 10 DAY, 1, 'Move-out walkthrough with tenant present'),
 (3,3,'MOVE_IN',  '2024-06-01', 1, 'Move-in walkthrough'),
 (4,3,'MOVE_OUT', CURDATE() - INTERVAL 26 DAY, 1, 'Move-out walkthrough');

INSERT INTO inspection_items (item_id, inspection_id, item_category, item_name, location, condition_rating, item_age_years, cause_noted, remarks) VALUES
 (1,1,'WALL_PAINT','Living room wall paint','Living room','GOOD',1,NULL,'Clean, no marks'),
 (2,1,'CARPET','Bedroom carpet','Bedroom','FAIR',8,NULL,'Light wear'),
 (3,2,'WALL_PAINT','Living room wall paint','Living room','DAMAGED',2,'TENANT_ACTION','Marker stains across one wall'),
 (4,2,'CARPET','Bedroom carpet','Bedroom','POOR',9,'NORMAL_USE','Flattened and faded high-traffic area'),
 (5,3,'WALL_PAINT','Hall wall paint','Hall','GOOD',0,NULL,NULL),
 (6,4,'COUNTERTOP','Kitchen countertop','Kitchen','DAMAGED',NULL,'UNKNOWN','Chip and burn mark; no baseline record for this item');

-- maintenance issues
INSERT INTO issues (issue_id, unit_id, tenant_id, category, title, description, severity, severity_source, status, escalation_level, created_at, acknowledged_at, sla_due_at, resolved_at) VALUES
 (1,1,1,'PLUMBING','Water leak under kitchen sink','Water leak under kitchen sink, floor is flooding','EMERGENCY','AUTO','IN_PROGRESS',0,
    NOW() - INTERVAL 90 MINUTE, NOW() - INTERVAL 75 MINUTE, NOW() + INTERVAL 150 MINUTE, NULL),
 (2,4,4,'GENERAL','Hallway light bulb not working','Light bulb in the hallway is not working','LOW','AUTO','ASSIGNED',0,
    NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 20 HOUR, NOW() + INTERVAL 6 DAY, NULL),
 (3,1,1,'PLUMBING','No water supply in bathroom','No water coming from the bathroom taps since yesterday','HIGH','AUTO','ASSIGNED',2,
    NOW() - INTERVAL 50 HOUR, NOW() - INTERVAL 49 HOUR, NOW() - INTERVAL 26 HOUR, NULL),
 (4,1,1,'GENERAL','Bedroom door will not close','Bedroom door is misaligned and will not close properly','MEDIUM','AUTO','RESOLVED',0,
    NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 5 DAY),
 (5,4,4,'GENERAL','Window latch broken','Window latch is broken and the window will not lock','MEDIUM','AUTO','RESOLVED',2,
    NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY);

INSERT INTO issue_status_history (issue_id, old_status, new_status, old_severity, new_severity, changed_by_user_id, change_reason, changed_at) VALUES
 (1,NULL,'OPEN',NULL,'EMERGENCY',NULL,'Issue created; classified by keyword rules', NOW() - INTERVAL 90 MINUTE),
 (1,'OPEN','IN_PROGRESS','EMERGENCY','EMERGENCY',6,'Vendor started work', NOW() - INTERVAL 60 MINUTE),
 (2,NULL,'OPEN',NULL,'LOW',NULL,'Issue created; classified by keyword rules', NOW() - INTERVAL 1 DAY),
 (2,'OPEN','ASSIGNED','LOW','LOW',1,'Vendor assigned', NOW() - INTERVAL 20 HOUR),
 (3,NULL,'OPEN',NULL,'HIGH',NULL,'Issue created; classified by keyword rules', NOW() - INTERVAL 50 HOUR),
 (3,'OPEN','ASSIGNED','HIGH','HIGH',1,'Vendor assigned', NOW() - INTERVAL 49 HOUR),
 (4,NULL,'OPEN',NULL,'MEDIUM',NULL,'Issue created; classified by keyword rules', NOW() - INTERVAL 6 DAY),
 (4,'OPEN','RESOLVED','MEDIUM','MEDIUM',8,'Work completed', NOW() - INTERVAL 5 DAY),
 (5,NULL,'OPEN',NULL,'MEDIUM',NULL,'Issue created; classified by keyword rules', NOW() - INTERVAL 10 DAY),
 (5,'OPEN','RESOLVED','MEDIUM','MEDIUM',8,'Work completed', NOW() - INTERVAL 6 DAY);

INSERT INTO escalations (issue_id, level, reason, triggered_at) VALUES
 (3,1,'50% of SLA elapsed without resolution', NOW() - INTERVAL 38 HOUR),
 (3,2,'SLA breached', NOW() - INTERVAL 26 HOUR),
 (5,1,'50% of SLA elapsed without resolution', NOW() - INTERVAL 8 DAY + INTERVAL 12 HOUR),
 (5,2,'SLA breached', NOW() - INTERVAL 7 DAY);

-- work orders
INSERT INTO work_orders (work_order_id, issue_id, inspection_item_id, vendor_id, status, assigned_at, assigned_by_user_id, accepted_at, started_at, completed_at, vendor_notes) VALUES
 (1,1,NULL,1,'IN_PROGRESS', NOW() - INTERVAL 80 MINUTE, 1, NOW() - INTERVAL 75 MINUTE, NOW() - INTERVAL 60 MINUTE, NULL, 'Main valve isolated; replacing pipe section'),
 (2,2,NULL,3,'ASSIGNED',    NOW() - INTERVAL 20 HOUR,   1, NULL, NULL, NULL, NULL),
 (3,3,NULL,1,'ASSIGNED',    NOW() - INTERVAL 49 HOUR,   1, NULL, NULL, NULL, NULL),
 (4,4,NULL,3,'COMPLETED',   NOW() - INTERVAL 6 DAY, 1, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 5 DAY - INTERVAL 3 HOUR, NOW() - INTERVAL 5 DAY, 'Door realigned and hinges replaced'),
 (5,5,NULL,3,'COMPLETED',   NOW() - INTERVAL 10 DAY, 1, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY, 'Latch replaced'),
 (6,NULL,3,3,'COMPLETED',   NOW() - INTERVAL 9 DAY, 1, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY, 'Repainted living room wall, marker stains removed');

INSERT INTO work_order_updates (work_order_id, status, note, created_by_user_id, created_at) VALUES
 (1,'ACCEPTED','On the way', 6, NOW() - INTERVAL 75 MINUTE),
 (1,'IN_PROGRESS','Main valve isolated; replacing pipe section', 6, NOW() - INTERVAL 60 MINUTE),
 (4,'COMPLETED','Door realigned and hinges replaced', 8, NOW() - INTERVAL 5 DAY),
 (6,'COMPLETED','Repainted living room wall, marker stains removed', 8, NOW() - INTERVAL 6 DAY);

-- repair costs
INSERT INTO repair_costs (cost_id, issue_id, work_order_id, inspection_item_id, vendor_id, entry_type, amount, cost_type, description, invoice_ref, incurred_at, entered_by_user_id) VALUES
 (1,4,4,NULL,3,'CHARGE',1800,'LABOR',   'Door alignment labour',        'INV-1001', NOW() - INTERVAL 5 DAY, 1),
 (2,4,4,NULL,3,'CHARGE', 700,'MATERIAL','Hinges and screws',            'INV-1001', NOW() - INTERVAL 5 DAY, 1),
 (3,5,5,NULL,3,'CHARGE',2200,'LABOR',   'Window latch replacement',     'INV-1002', NOW() - INTERVAL 6 DAY, 1),
 (4,NULL,6,3,3,'CHARGE',3000,'LABOR',   'Repaint labour, A102 living room','INV-1004', NOW() - INTERVAL 6 DAY, 1),
 (5,NULL,6,3,3,'CHARGE',1200,'MATERIAL','Paint and primer',             'INV-1004', NOW() - INTERVAL 6 DAY, 1),
 (6,1,1,NULL,1,'CHARGE',2500,'MATERIAL','Replacement pipe section',     'INV-1005', NOW() - INTERVAL 45 MINUTE, 1);

-- deposit disputes
INSERT INTO deposit_disputes (dispute_id, lease_id, tenant_id, deposit_held, status, opened_at, resolved_at) VALUES
 (1,2,2,40000,'RESOLVED',             NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 2 DAY),
 (2,3,3,60000,'AWAITING_HUMAN_REVIEW',NOW() - INTERVAL 5 DAY, NULL);

INSERT INTO dispute_items (dispute_item_id, dispute_id, move_out_item_id, move_in_item_id, claimed_amount, tenant_position, tenant_comment,
                           system_classification, applied_rule_id, rule_trace, operator_decision, decided_amount, decided_by_user_id, decided_at, decision_notes) VALUES
 (1,1,3,1,6000,'CONTESTED','The marks are smaller than described and I would like the cost to match the actual repair.',
    'DAMAGE',5,'{"rule_id":5,"rule_name":"Wall paint damaged by tenant action","matched":["item_category","move_out_condition","cause"],"baseline_found":true,"move_out_photos":1}',
    'CHARGE_REDUCED',4200,1,NOW() - INTERVAL 2 DAY,
    'Reduced from 6000 to the ledger repair cost of 4200 (cost entries 4 and 5, invoice INV-1004). Evidence: move-in photo 1, move-out photo 2.'),
 (2,1,4,2,12000,'CONTESTED','The carpet was already old and worn when I moved in.',
    'WEAR_AND_TEAR',1,'{"rule_id":1,"rule_name":"Carpet past useful life, poor condition","matched":["item_category","min_age_years","move_out_condition"],"baseline_found":true,"move_out_photos":1}',
    'CHARGE_WAIVED',0,1,NOW() - INTERVAL 2 DAY,
    'Carpet was 8 years old at move-in and 9 at move-out; classified as wear and tear under rule 1. Evidence: move-in photo 3, move-out photo 4.'),
 (3,2,6,NULL,8500,'CONTESTED','I do not recall this damage; it was not on any list when I moved in.',
    'NEEDS_HUMAN_REVIEW',NULL,'{"reason_code":"MISSING_MOVE_IN_BASELINE","baseline_found":false,"cause_noted":"UNKNOWN","move_out_photos":1}',
    NULL,NULL,NULL,NULL,NULL);

INSERT INTO evidence_files (evidence_id, inspection_item_id, issue_id, dispute_item_id, s3_key, file_name, content_type, size_bytes, sha256, captured_at, uploaded_by_user_id) VALUES
 (1,1,NULL,NULL,'evidence/lease-2/move-in/living-room-wall.jpg',  'living-room-wall.jpg',  'image/jpeg',2481920,REPEAT('a',64),'2025-03-01 10:15:00',1),
 (2,3,NULL,1,   'evidence/lease-2/move-out/living-room-wall.jpg', 'living-room-wall.jpg',  'image/jpeg',2650112,REPEAT('b',64), NOW() - INTERVAL 10 DAY,1),
 (3,2,NULL,NULL,'evidence/lease-2/move-in/bedroom-carpet.jpg',    'bedroom-carpet.jpg',    'image/jpeg',2210048,REPEAT('c',64),'2025-03-01 10:25:00',1),
 (4,4,NULL,2,   'evidence/lease-2/move-out/bedroom-carpet.jpg',   'bedroom-carpet.jpg',    'image/jpeg',2399232,REPEAT('d',64), NOW() - INTERVAL 10 DAY,1),
 (5,NULL,1,NULL,'evidence/issues/1/kitchen-sink-leak.jpg',        'kitchen-sink-leak.jpg', 'image/jpeg',1875456,REPEAT('e',64), NOW() - INTERVAL 90 MINUTE,2),
 (6,6,NULL,3,   'evidence/lease-3/move-out/kitchen-countertop.jpg','kitchen-countertop.jpg','image/jpeg',2012160,REPEAT('f',64), NOW() - INTERVAL 26 DAY,1);

-- review queue
INSERT INTO review_queue (dispute_item_id, reason_code, reason_text, status) VALUES
 (3,'MISSING_MOVE_IN_BASELINE',
    'No move-in inspection record exists for the kitchen countertop and the cause of damage is recorded as UNKNOWN. The system cannot classify this item as wear and tear or damage, so a person must review the evidence.',
    'PENDING');

INSERT INTO dispute_events (dispute_id, dispute_item_id, event_type, actor_type, actor_user_id, detail, created_at) VALUES
 (1,NULL,'DISPUTE_OPENED','ADMIN',1,'Move-out claims raised for 2 items', NOW() - INTERVAL 8 DAY),
 (1,1,'ITEM_CLASSIFIED','SYSTEM',NULL,'Classified DAMAGE by rule 5', NOW() - INTERVAL 8 DAY),
 (1,2,'ITEM_CLASSIFIED','SYSTEM',NULL,'Classified WEAR_AND_TEAR by rule 1', NOW() - INTERVAL 8 DAY),
 (1,1,'TENANT_CONTESTED','TENANT',3,'Tenant contested the claimed amount', NOW() - INTERVAL 7 DAY),
 (1,2,'TENANT_CONTESTED','TENANT',3,'Tenant contested the claim', NOW() - INTERVAL 7 DAY),
 (1,1,'OPERATOR_DECISION','ADMIN',1,'CHARGE_REDUCED to 4200', NOW() - INTERVAL 2 DAY),
 (1,2,'OPERATOR_DECISION','ADMIN',1,'CHARGE_WAIVED', NOW() - INTERVAL 2 DAY),
 (1,NULL,'DISPUTE_RESOLVED','ADMIN',1,'All items decided', NOW() - INTERVAL 2 DAY),
 (2,NULL,'DISPUTE_OPENED','ADMIN',1,'Move-out claim raised for 1 item', NOW() - INTERVAL 5 DAY),
 (2,3,'ROUTED_TO_HUMAN_REVIEW','SYSTEM',NULL,'MISSING_MOVE_IN_BASELINE', NOW() - INTERVAL 5 DAY);

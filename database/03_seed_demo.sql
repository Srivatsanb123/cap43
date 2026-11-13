USE cap43;
SET @pending = 'PENDING_SET_BY_APP';

-- users
INSERT INTO users (user_id, email, password_hash, role, full_name, phone) VALUES
 (1,  'admin@propledger.com',         @pending, 'ADMIN',  'Operations Admin',     '9876500001'),
 (2,  'sarah.admin@propledger.com',   @pending, 'ADMIN',  'Sarah Jenkins',        '9876500002'),
 (3,  'priya.raman@propledger.com',   @pending, 'TENANT', 'Priya Raman',          '9876500003'),
 (4,  'arun.kumar@propledger.com',    @pending, 'TENANT', 'Arun Kumar',           '9876500004'),
 (5,  'meena.iyer@propledger.com',    @pending, 'TENANT', 'Meena Iyer',           '9876500005'),
 (6,  'karthik.s@propledger.com',     @pending, 'TENANT', 'Karthik Subramanian',  '9876500006'),
 (7,  'divya.nair@propledger.com',    @pending, 'TENANT', 'Divya Nair',           '9876500007'),
 (8,  'rohan.mehta@propledger.com',   @pending, 'TENANT', 'Rohan Mehta',          '9876500008'),
 (9,  'ananya.sen@propledger.com',    @pending, 'TENANT', 'Ananya Sen',           '9876500009'),
 (10, 'vikram.patel@propledger.com',  @pending, 'TENANT', 'Vikram Patel',         '9876500010'),
 (11, 'neha.sharma@propledger.com',   @pending, 'TENANT', 'Neha Sharma',          '9876500011'),
 (12, 'rajesh.verma@propledger.com',  @pending, 'TENANT', 'Rajesh Verma',         '9876500012'),
 (13, 'sneha.reddy@propledger.com',   @pending, 'TENANT', 'Sneha Reddy',          '9876500013'),
 (14, 'amit.joshi@propledger.com',    @pending, 'TENANT', 'Amit Joshi',           '9876500014'),
 (15, 'contact@fixitplumbing.com',    @pending, 'VENDOR', 'FixIt Plumbing Ltd',   '9876500015'),
 (16, 'support@brightspark.com',      @pending, 'VENDOR', 'BrightSpark Electrics','9876500016'),
 (17, 'service@arcticair.com',        @pending, 'VENDOR', 'ArcticAir HVAC Ltd',   '9876500017'),
 (18, 'ops@timbercraft.com',          @pending, 'VENDOR', 'TimberCraft Carpentry','9876500018'),
 (19, 'info@purecoat.com',            @pending, 'VENDOR', 'PureCoat Painters',    '9876500019'),
 (20, 'dispatch@metrohandy.com',      @pending, 'VENDOR', 'Metro Handyman Co',    '9876500020');

-- properties
INSERT INTO properties (property_id, name, address_line, city, state, postal_code) VALUES
 (1, 'Maple Heights',       '12 Park Avenue, Anna Nagar',    'Chennai',    'Tamil Nadu',  '600040'),
 (2, 'Lakeview Towers',     '48 Lake Road, Adyar',           'Chennai',    'Tamil Nadu',  '600020'),
 (3, 'Palm Grove Residency','77 4th Block, Koramangala',     'Bengaluru',  'Karnataka',   '560034'),
 (4, 'CyberPark Enclave',   '102 Cyber City Road, Madhapur', 'Hyderabad',  'Telangana',   '500081'),
 (5, 'Orchid Pavilion',     '15 Turner Road, Bandra West',   'Mumbai',     'Maharashtra', '400050');

-- units
INSERT INTO units (unit_id, property_id, unit_number, floor_no, bedrooms) VALUES
 (1,  1, 'A101', 1, 2),
 (2,  1, 'A102', 1, 2),
 (3,  1, 'A201', 2, 3),
 (4,  1, 'A202', 2, 1),
 (5,  2, 'B101', 1, 2),
 (6,  2, 'B201', 2, 3),
 (7,  2, 'B301', 3, 3),
 (8,  3, 'C101', 1, 2),
 (9,  3, 'C202', 2, 2),
 (10, 3, 'C303', 3, 3),
 (11, 4, 'D101', 1, 1),
 (12, 4, 'D201', 2, 2),
 (13, 4, 'D401', 4, 3),
 (14, 5, 'E101', 1, 2),
 (15, 5, 'E202', 2, 2),
 (16, 5, 'E301', 3, 3);

-- tenants
INSERT INTO tenants (tenant_id, user_id, tenant_ref) VALUES
 (1,  3,  'TN-0001'),
 (2,  4,  'TN-0002'),
 (3,  5,  'TN-0003'),
 (4,  6,  'TN-0004'),
 (5,  7,  'TN-0005'),
 (6,  8,  'TN-0006'),
 (7,  9,  'TN-0007'),
 (8,  10, 'TN-0008'),
 (9,  11, 'TN-0009'),
 (10, 12, 'TN-0010'),
 (11, 13, 'TN-0011'),
 (12, 14, 'TN-0012');

-- vendors
INSERT INTO vendors (vendor_id, user_id, company_name, trade) VALUES
 (1, 15, 'FixIt Plumbing Ltd',    'PLUMBING'),
 (2, 16, 'BrightSpark Electrics', 'ELECTRICAL'),
 (3, 17, 'ArcticAir HVAC Ltd',    'HVAC'),
 (4, 18, 'TimberCraft Carpentry', 'CARPENTRY'),
 (5, 19, 'PureCoat Painters',     'PAINTING'),
 (6, 20, 'Metro Handyman Co',     'GENERAL');

-- leases
INSERT INTO leases (lease_id, unit_id, tenant_id, start_date, end_date, monthly_rent, deposit_amount, status, move_out_date) VALUES
 (1,  1,  1,  '2025-01-01', '2026-12-31', 26000, 52000, 'ACTIVE',     NULL),
 (2,  2,  2,  '2024-03-01', CURDATE() - INTERVAL 12 DAY, 22000, 44000, 'ENDED', CURDATE() - INTERVAL 12 DAY),
 (3,  6,  3,  '2024-06-01', CURDATE() - INTERVAL 25 DAY, 32000, 64000, 'ENDED', CURDATE() - INTERVAL 25 DAY),
 (4,  4,  4,  '2025-02-01', '2027-01-31', 24000, 48000, 'ACTIVE',     NULL),
 (5,  8,  5,  '2025-01-15', '2026-01-14', 28000, 56000, 'ACTIVE',     NULL),
 (6,  9,  6,  '2024-02-01', CURDATE() - INTERVAL 18 DAY, 30000, 60000, 'ENDED', CURDATE() - INTERVAL 18 DAY),
 (7,  11, 7,  '2025-04-01', '2026-03-31', 19000, 38000, 'ACTIVE',     NULL),
 (8,  12, 8,  '2024-01-01', CURDATE() - INTERVAL 8 DAY,  27000, 54000, 'ENDED', CURDATE() - INTERVAL 8 DAY),
 (9,  14, 9,  '2025-03-01', '2027-02-28', 35000, 70000, 'ACTIVE',     NULL),
 (10, 15, 10, '2025-05-01', '2026-04-30', 36000, 72000, 'ACTIVE',     NULL),
 (11, 3,  11, '2024-08-01', CURDATE() - INTERVAL 4 DAY,  31000, 62000, 'ENDED', CURDATE() - INTERVAL 4 DAY),
 (12, 16, 12, '2025-02-15', '2026-02-14', 40000, 80000, 'ACTIVE',     NULL),
 (13, 5,  1,  '2023-01-01', '2024-01-01', 21000, 42000, 'TERMINATED', '2023-11-15'),
 (14, 10, 5,  '2023-05-01', '2024-04-30', 29000, 58000, 'TERMINATED', '2024-03-01');

-- inspections
INSERT INTO inspections (inspection_id, lease_id, inspection_type, inspected_on, inspector_user_id, notes) VALUES
 (1,  2,  'MOVE_IN',  '2024-03-01', 1, 'Initial walkthrough - unit handed over in pristine order'),
 (2,  2,  'MOVE_OUT', CURDATE() - INTERVAL 12 DAY, 1, 'Move-out walkthrough conducted with tenant present'),
 (3,  3,  'MOVE_IN',  '2024-06-01', 1, 'Move-in baseline walkthrough'),
 (4,  3,  'MOVE_OUT', CURDATE() - INTERVAL 25 DAY, 1, 'Move-out inspection noting heavy kitchen usage'),
 (5,  6,  'MOVE_IN',  '2024-02-01', 2, 'Detailed check: appliances and wall paint clean'),
 (6,  6,  'MOVE_OUT', CURDATE() - INTERVAL 18 DAY, 2, 'Move-out check: bedroom floor scratch and carpet wear'),
 (7,  8,  'MOVE_IN',  '2024-01-01', 1, 'Move-in baseline verified'),
 (8,  8,  'MOVE_OUT', CURDATE() - INTERVAL 8 DAY,  1, 'Move-out walkthrough: bathroom vanity mirror crack'),
 (9,  11, 'MOVE_IN',  '2024-08-01', 2, 'Handover completed in good state'),
 (10, 11, 'MOVE_OUT', CURDATE() - INTERVAL 4 DAY,  2, 'Move-out review: living room paint and kitchen cabinet latch');

-- inspection items
INSERT INTO inspection_items (item_id, inspection_id, item_category, item_name, location, condition_rating, item_age_years, cause_noted, remarks) VALUES
 (1,  1,  'WALL_PAINT',  'Living room paint',     'Living Room', 'GOOD',    1,    NULL,            'Fresh paint, no scuffs'),
 (2,  1,  'CARPET',      'Master bedroom carpet', 'Bedroom',     'FAIR',    8,    NULL,            'Clean, slight pile wear in doorway'),
 (3,  2,  'WALL_PAINT',  'Living room paint',     'Living Room', 'DAMAGED', 2,    'TENANT_ACTION', 'Deep marker drawing across wall'),
 (4,  2,  'CARPET',      'Master bedroom carpet', 'Bedroom',     'POOR',    9,    'NORMAL_USE',    'Faded and flattened traffic lane'),
 (5,  3,  'WALL_PAINT',  'Hallway paint',         'Hallway',     'GOOD',    0,    NULL,            'Newly painted coat'),
 (6,  4,  'COUNTERTOP',  'Kitchen quartz slab',   'Kitchen',     'DAMAGED', NULL, 'UNKNOWN',       'Chip and burn mark near stove; no baseline'),
 (7,  5,  'FLOORING',    'Living room hardwood',  'Living Room', 'GOOD',    3,    NULL,            'Polished hardwood with zero deep scratches'),
 (8,  5,  'CARPET',      'Guest bedroom carpet',  'Bedroom',     'FAIR',    9,    NULL,            'Carpet aged 9 years, moderate fading'),
 (9,  6,  'FLOORING',    'Living room hardwood',  'Living Room', 'DAMAGED', 4,    'TENANT_ACTION', 'Gouge marks from dragging heavy furniture'),
 (10, 6,  'CARPET',      'Guest bedroom carpet',  'Bedroom',     'POOR',    10,   'NORMAL_USE',    'Even wear over useful life'),
 (11, 7,  'FIXTURE',     'Bathroom vanity mirror','Bathroom',    'GOOD',    2,    NULL,            'Clear reflection, intact frame'),
 (12, 7,  'APPLIANCE',   'Kitchen microwave oven','Kitchen',     'FAIR',    11,   NULL,            'Working condition, aged 11 years'),
 (13, 8,  'FIXTURE',     'Bathroom vanity mirror','Bathroom',    'DAMAGED', 3,    'TENANT_ACTION', 'Spiderweb fracture across corner'),
 (14, 8,  'APPLIANCE',   'Kitchen microwave oven','Kitchen',     'POOR',    12,   'NORMAL_USE',    'Turntable motor stopped due to age'),
 (15, 9,  'WALL_PAINT',  'Dining area paint',     'Dining',      'GOOD',    1,    NULL,            'Clean surface'),
 (16, 9,  'FIXTURE',     'Kitchen cabinet latch', 'Kitchen',     'GOOD',    2,    NULL,            'Smooth latch action'),
 (17, 10, 'WALL_PAINT',  'Dining area paint',     'Dining',      'FAIR',    3,    'NORMAL_USE',    'Minor furniture rub, natural aging'),
 (18, 10, 'FIXTURE',     'Kitchen cabinet latch', 'Kitchen',     'DAMAGED', 3,    'TENANT_ACTION', 'Hinge pulled off mounting surface'),
 (19, 1,  'FIXTURE',     'Balcony sliding lock',  'Balcony',     'GOOD',    1,    NULL,            'Locks securely'),
 (20, 2,  'FIXTURE',     'Balcony sliding lock',  'Balcony',     'DAMAGED', 2,    'TENANT_ACTION', 'Lever snapped off');

-- maintenance issues
INSERT INTO issues (issue_id, unit_id, tenant_id, category, title, description, severity, severity_source, status, escalation_level, created_at, acknowledged_at, sla_due_at, resolved_at) VALUES
 (1,  1,  1,  'PLUMBING',   'Active water pipe burst under kitchen sink',   'Water is gushing out under the kitchen sink, floor is flooding',               'EMERGENCY', 'AUTO', 'IN_PROGRESS', 0, NOW() - INTERVAL 110 MINUTE, NOW() - INTERVAL 95 MINUTE, NOW() + INTERVAL 130 MINUTE, NULL),
 (2,  4,  4,  'GENERAL',    'Hallway ceiling light bulb flickering',        'Light bulb in the main entrance hallway has burned out completely',            'LOW',       'AUTO', 'ASSIGNED',    0, NOW() - INTERVAL 2 DAY,        NOW() - INTERVAL 36 HOUR,       NOW() + INTERVAL 5 DAY,         NULL),
 (3,  1,  1,  'PLUMBING',   'Complete water outage in bathroom taps',       'No water supply from either bathroom tap or showerhead',                       'HIGH',      'AUTO', 'ASSIGNED',    2, NOW() - INTERVAL 52 HOUR,       NOW() - INTERVAL 50 HOUR,       NOW() - INTERVAL 28 HOUR,       NULL),
 (4,  1,  1,  'GENERAL',    'Bedroom door misaligned and jammed',           'Door cannot be closed or latched shut without forceful impact',                'MEDIUM',    'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 7 DAY,        NOW() - INTERVAL 7 DAY,         NOW() - INTERVAL 4 DAY,         NOW() - INTERVAL 5 DAY),
 (5,  4,  4,  'GENERAL',    'Window security latch broken',                 'Ground floor window will not latch shut, security risk',                       'MEDIUM',    'AUTO', 'RESOLVED',    2, NOW() - INTERVAL 12 DAY,       NOW() - INTERVAL 12 DAY,        NOW() - INTERVAL 9 DAY,         NOW() - INTERVAL 8 DAY),
 (6,  8,  5,  'ELECTRICAL', 'Sparking wall outlet in living room',          'Sparking sound and burning plastic odor from socket when plugging in iron',    'EMERGENCY', 'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 3 DAY,        NOW() - INTERVAL 3 DAY + INTERVAL 10 MINUTE, NOW() - INTERVAL 3 DAY + INTERVAL 4 HOUR, NOW() - INTERVAL 3 DAY + INTERVAL 2 HOUR),
 (7,  9,  6,  'HVAC',       'Master bedroom AC not cooling',                'AC blower runs but only delivers warm air on hot afternoon',                   'MEDIUM',    'AUTO', 'IN_PROGRESS', 0, NOW() - INTERVAL 30 HOUR,       NOW() - INTERVAL 28 HOUR,       NOW() + INTERVAL 42 HOUR,       NULL),
 (8,  11, 7,  'PLUMBING',   'Toilet flush mechanism running continuously',  'Water constantly runs down into the toilet bowl wasting water',                'MEDIUM',    'AUTO', 'ASSIGNED',    0, NOW() - INTERVAL 18 HOUR,       NOW() - INTERVAL 15 HOUR,       NOW() + INTERVAL 54 HOUR,       NULL),
 (9,  12, 8,  'ELECTRICAL', 'Main circuit breaker keeps tripping',          'Trips every time kitchen kettle or microwave is turned on',                    'HIGH',      'AUTO', 'IN_PROGRESS', 1, NOW() - INTERVAL 20 HOUR,       NOW() - INTERVAL 18 HOUR,       NOW() + INTERVAL 4 HOUR,        NULL),
 (10, 14, 9,  'APPLIANCE',  'Dishwasher leaking water from bottom door seal','Puddle forms on kitchen tiles during active wash cycle',                       'MEDIUM',    'AUTO', 'ASSIGNED',    0, NOW() - INTERVAL 40 HOUR,       NOW() - INTERVAL 32 HOUR,       NOW() + INTERVAL 32 HOUR,       NULL),
 (11, 15, 10, 'STRUCTURAL', 'Balcony ceiling plaster flaking and falling', 'Chunks of painted plaster fell down onto the patio table',                    'MEDIUM',    'AUTO', 'OPEN',        0, NOW() - INTERVAL 8 HOUR,        NULL,                           NOW() + INTERVAL 64 HOUR,       NULL),
 (12, 16, 12, 'PLUMBING',   'Drainage backup in laundry wash basin',        'Water drains very slowly and produces foul gurgling sound',                    'LOW',       'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 6 DAY,        NOW() - INTERVAL 5 DAY,         NOW() + INTERVAL 1 DAY,         NOW() - INTERVAL 3 DAY),
 (13, 8,  5,  'HVAC',       'Central ventilation vent rattling loudly',     'Vibration noise through air vent disturbing sleep',                            'LOW',       'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 9 DAY,        NOW() - INTERVAL 8 DAY,         NOW() - INTERVAL 2 DAY,         NOW() - INTERVAL 6 DAY),
 (14, 11, 7,  'GENERAL',    'Kitchen drawer track detached',                'Silverware drawer fell off roller runner',                                     'LOW',       'AUTO', 'ASSIGNED',    0, NOW() - INTERVAL 24 HOUR,       NOW() - INTERVAL 12 HOUR,       NOW() + INTERVAL 6 DAY,         NULL),
 (15, 2,  2,  'PLUMBING',   'Water heater thermal cut-off tripped',         'No hot water coming out of bathroom showers',                                  'HIGH',      'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 15 DAY,       NOW() - INTERVAL 15 DAY,        NOW() - INTERVAL 14 DAY,        NOW() - INTERVAL 14 DAY + INTERVAL 6 HOUR),
 (16, 3,  11, 'ELECTRICAL', 'Exhaust fan stopped spinning',                 'Kitchen chimney exhaust fan will not start',                                   'MEDIUM',    'AUTO', 'RESOLVED',    1, NOW() - INTERVAL 14 DAY,       NOW() - INTERVAL 14 DAY,        NOW() - INTERVAL 11 DAY,        NOW() - INTERVAL 11 DAY + INTERVAL 4 HOUR),
 (17, 6,  3,  'PLUMBING',   'Main water line pressure very weak',           'Water trickles from all taps across unit',                                     'HIGH',      'AUTO', 'RESOLVED',    2, NOW() - INTERVAL 22 DAY,       NOW() - INTERVAL 21 DAY,        NOW() - INTERVAL 20 DAY,        NOW() - INTERVAL 18 DAY),
 (18, 9,  6,  'GENERAL',    'Wardrobe sliding mirror door stuck',           'Bottom runner jammed and will not slide open',                                 'LOW',       'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 20 DAY,       NOW() - INTERVAL 19 DAY,        NOW() - INTERVAL 13 DAY,        NOW() - INTERVAL 17 DAY),
 (19, 12, 8,  'PLUMBING',   'Showerhead spraying water sideways from joint','Loose swivel nut causing spray onto bathroom drywall',                         'LOW',       'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 16 DAY,       NOW() - INTERVAL 15 DAY,        NOW() - INTERVAL 9 DAY,         NOW() - INTERVAL 14 DAY),
 (20, 14, 9,  'HVAC',       'AC thermostat screen unreadable / blank',      'Thermostat remote and wall unit display completely black',                     'MEDIUM',    'AUTO', 'RESOLVED',    0, NOW() - INTERVAL 11 DAY,       NOW() - INTERVAL 11 DAY,        NOW() - INTERVAL 8 DAY,         NOW() - INTERVAL 9 DAY);

-- issue status history
INSERT INTO issue_status_history (issue_id, old_status, new_status, old_severity, new_severity, changed_by_user_id, change_reason, changed_at) VALUES
 (1,  NULL,          'OPEN',        NULL,        'EMERGENCY', NULL, 'Issue submitted by tenant; auto-classified', NOW() - INTERVAL 110 MINUTE),
 (1,  'OPEN',        'IN_PROGRESS', 'EMERGENCY', 'EMERGENCY', 15,   'FixIt Plumbing dispatched to isolate leak', NOW() - INTERVAL 90 MINUTE),
 (2,  NULL,          'OPEN',        NULL,        'LOW',       NULL, 'Issue submitted by tenant',                  NOW() - INTERVAL 2 DAY),
 (2,  'OPEN',        'ASSIGNED',    'LOW',       'LOW',       1,    'Assigned to Metro Handyman',                 NOW() - INTERVAL 36 HOUR),
 (3,  NULL,          'OPEN',        NULL,        'HIGH',      NULL, 'Issue submitted by tenant',                  NOW() - INTERVAL 52 HOUR),
 (3,  'OPEN',        'ASSIGNED',    'HIGH',      'HIGH',      1,    'Vendor notified',                            NOW() - INTERVAL 50 HOUR),
 (4,  NULL,          'OPEN',        NULL,        'MEDIUM',    NULL, 'Issue reported',                             NOW() - INTERVAL 7 DAY),
 (4,  'OPEN',        'RESOLVED',    'MEDIUM',    'MEDIUM',    18,   'Door planed and rehung',                     NOW() - INTERVAL 5 DAY),
 (5,  NULL,          'OPEN',        NULL,        'MEDIUM',    NULL, 'Issue reported',                             NOW() - INTERVAL 12 DAY),
 (5,  'OPEN',        'RESOLVED',    'MEDIUM',    'MEDIUM',    20,   'Latch replaced',                             NOW() - INTERVAL 8 DAY),
 (6,  NULL,          'OPEN',        NULL,        'EMERGENCY', NULL, 'Issue reported',                             NOW() - INTERVAL 3 DAY),
 (6,  'OPEN',        'RESOLVED',    'EMERGENCY', 'EMERGENCY', 16,   'Socket replaced and circuit verified',       NOW() - INTERVAL 3 DAY + INTERVAL 2 HOUR),
 (7,  NULL,          'OPEN',        NULL,        'MEDIUM',    NULL, 'Issue reported',                             NOW() - INTERVAL 30 HOUR),
 (7,  'OPEN',        'IN_PROGRESS', 'MEDIUM',    'MEDIUM',    17,   'Refrigerant level inspection underway',      NOW() - INTERVAL 24 HOUR),
 (9,  NULL,          'OPEN',        NULL,        'HIGH',      NULL, 'Issue reported',                             NOW() - INTERVAL 20 HOUR),
 (9,  'OPEN',        'IN_PROGRESS', 'HIGH',      'HIGH',      16,   'Troubleshooting distribution panel',         NOW() - INTERVAL 16 HOUR),
 (12, NULL,          'OPEN',        NULL,        'LOW',       NULL, 'Issue reported',                             NOW() - INTERVAL 6 DAY),
 (12, 'OPEN',        'RESOLVED',    'LOW',       'LOW',       15,   'Trap cleaned and cleared',                   NOW() - INTERVAL 3 DAY),
 (17, NULL,          'OPEN',        NULL,        'HIGH',      NULL, 'Issue reported',                             NOW() - INTERVAL 22 DAY),
 (17, 'OPEN',        'RESOLVED',    'HIGH',      'HIGH',      15,   'Booster valve replaced',                     NOW() - INTERVAL 18 DAY);

-- escalations
INSERT INTO escalations (issue_id, level, reason, triggered_at) VALUES
 (3,  1, '50% of SLA elapsed without resolution', NOW() - INTERVAL 40 HOUR),
 (3,  2, 'SLA breached - unresolved past 24 hours',NOW() - INTERVAL 28 HOUR),
 (5,  1, '50% of SLA elapsed without resolution', NOW() - INTERVAL 10 DAY),
 (5,  2, 'SLA breached',                           NOW() - INTERVAL 9 DAY),
 (9,  1, '50% of SLA elapsed without resolution', NOW() - INTERVAL 8 HOUR),
 (16, 1, '50% of SLA elapsed without resolution', NOW() - INTERVAL 12 DAY),
 (17, 1, '50% of SLA elapsed without resolution', NOW() - INTERVAL 21 DAY),
 (17, 2, 'SLA breached - delayed part delivery',  NOW() - INTERVAL 20 DAY);

-- work orders
INSERT INTO work_orders (work_order_id, issue_id, inspection_item_id, vendor_id, status, assigned_at, assigned_by_user_id, accepted_at, started_at, completed_at, vendor_notes) VALUES
 (1,  1,  NULL, 1, 'IN_PROGRESS', NOW() - INTERVAL 100 MINUTE, 1, NOW() - INTERVAL 95 MINUTE, NOW() - INTERVAL 90 MINUTE, NULL, 'Isolated stop valve; installing new coupling'),
 (2,  2,  NULL, 6, 'ASSIGNED',    NOW() - INTERVAL 36 HOUR,   1, NULL, NULL, NULL, NULL),
 (3,  3,  NULL, 1, 'ASSIGNED',    NOW() - INTERVAL 50 HOUR,   1, NULL, NULL, NULL, NULL),
 (4,  4,  NULL, 4, 'COMPLETED',   NOW() - INTERVAL 7 DAY,     1, NOW() - INTERVAL 7 DAY,     NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 5 DAY, 'Planed lower edge of door; aligned strike plate'),
 (5,  5,  NULL, 6, 'COMPLETED',   NOW() - INTERVAL 12 DAY,    1, NOW() - INTERVAL 11 DAY,    NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY, 'Replaced damaged window catch lock'),
 (6,  6,  NULL, 2, 'COMPLETED',   NOW() - INTERVAL 3 DAY,     1, NOW() - INTERVAL 3 DAY,     NOW() - INTERVAL 3 DAY + INTERVAL 30 MINUTE, NOW() - INTERVAL 3 DAY + INTERVAL 2 HOUR, 'Replaced 16A modular socket and checked earthing'),
 (7,  7,  NULL, 3, 'IN_PROGRESS', NOW() - INTERVAL 28 HOUR,   1, NOW() - INTERVAL 26 HOUR,   NOW() - INTERVAL 24 HOUR, NULL, 'Leak test on evaporator coil in progress'),
 (8,  8,  NULL, 1, 'ASSIGNED',    NOW() - INTERVAL 15 HOUR,   1, NULL, NULL, NULL, NULL),
 (9,  9,  NULL, 2, 'IN_PROGRESS', NOW() - INTERVAL 18 HOUR,   1, NOW() - INTERVAL 17 HOUR,   NOW() - INTERVAL 16 HOUR, NULL, 'Replaced 32A MCB; testing appliance load'),
 (10, 10, NULL, 6, 'ASSIGNED',    NOW() - INTERVAL 32 HOUR,   1, NULL, NULL, NULL, NULL),
 (11, 12, NULL, 1, 'COMPLETED',   NOW() - INTERVAL 5 DAY,     1, NOW() - INTERVAL 5 DAY,     NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY, 'Cleared hair and soap buildup from bottle trap'),
 (12, 13, NULL, 3, 'COMPLETED',   NOW() - INTERVAL 8 DAY,     1, NOW() - INTERVAL 8 DAY,     NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY, 'Damped motor housing mounting pads'),
 (13, NULL, 3,  5, 'COMPLETED',   NOW() - INTERVAL 10 DAY,    1, NOW() - INTERVAL 10 DAY,    NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY, 'Repainted living room accent wall, 2 coats'),
 (14, NULL, 9,  4, 'COMPLETED',   NOW() - INTERVAL 15 DAY,    1, NOW() - INTERVAL 14 DAY,    NOW() - INTERVAL 13 DAY,NOW() - INTERVAL 12 DAY,'Sanded deep floor gouges and applied polyurethane seal');

-- work order updates
INSERT INTO work_order_updates (work_order_id, status, note, created_by_user_id, created_at) VALUES
 (1,  'ACCEPTED',    'Plumbing crew en route with tools and parts',        15, NOW() - INTERVAL 95 MINUTE),
 (1,  'IN_PROGRESS', 'Shut off sub-riser; removing cracked pipe section',  15, NOW() - INTERVAL 90 MINUTE),
 (4,  'COMPLETED',   'Door hangs square and latches with zero resistance', 18, NOW() - INTERVAL 5 DAY),
 (6,  'COMPLETED',   'New socket tested with 2kW resistive load, normal',  16, NOW() - INTERVAL 3 DAY + INTERVAL 2 HOUR),
 (7,  'IN_PROGRESS', 'Checking pressure gauge readings on outdoor unit',   17, NOW() - INTERVAL 24 HOUR),
 (9,  'IN_PROGRESS', 'Isolating circuit branch 4 to verify ground fault',  16, NOW() - INTERVAL 16 HOUR),
 (13, 'COMPLETED',   'Finished living room wall paint with matching shade',19, NOW() - INTERVAL 8 DAY),
 (14, 'COMPLETED',   'Gouges filled and refinished; uniform gloss level',  18, NOW() - INTERVAL 12 DAY);

-- repair costs
INSERT INTO repair_costs (cost_id, issue_id, work_order_id, inspection_item_id, vendor_id, entry_type, amount, cost_type, description, invoice_ref, incurred_at, entered_by_user_id) VALUES
 (1,  4,    4,    NULL, 4, 'CHARGE', 1800.00, 'LABOR',    'Door plane and realignment labour',             'INV-2024-001', NOW() - INTERVAL 5 DAY, 1),
 (2,  4,    4,    NULL, 4, 'CHARGE',  650.00, 'MATERIAL', 'Brass hinges and mounting anchors',             'INV-2024-001', NOW() - INTERVAL 5 DAY, 1),
 (3,  5,    5,    NULL, 6, 'CHARGE', 2200.00, 'LABOR',    'Window security latch replacement labour',      'INV-2024-002', NOW() - INTERVAL 8 DAY, 1),
 (4,  5,    5,    NULL, 6, 'CHARGE',  850.00, 'MATERIAL', 'Stainless casement latch hardware',             'INV-2024-002', NOW() - INTERVAL 8 DAY, 1),
 (5,  6,    6,    NULL, 2, 'CHARGE', 1500.00, 'LABOR',    'Emergency electrician callout & inspection',    'INV-2024-003', NOW() - INTERVAL 3 DAY, 1),
 (6,  6,    6,    NULL, 2, 'CHARGE',  950.00, 'MATERIAL', 'Modular 16A switch socket and plate',           'INV-2024-003', NOW() - INTERVAL 3 DAY, 1),
 (7,  12,   11,   NULL, 1, 'CHARGE', 1400.00, 'LABOR',    'Plumbing trap dismantling and clearance',       'INV-2024-004', NOW() - INTERVAL 3 DAY, 1),
 (8,  13,   12,   NULL, 3, 'CHARGE', 2500.00, 'LABOR',    'AC ducting anti-vibration damping service',     'INV-2024-005', NOW() - INTERVAL 6 DAY, 1),
 (9,  NULL, 13,   3,    5, 'CHARGE', 3200.00, 'LABOR',    'Living room accent wall repainting labour',     'INV-2024-006', NOW() - INTERVAL 8 DAY, 1),
 (10, NULL, 13,   3,    5, 'CHARGE', 1400.00, 'MATERIAL', 'Emulsion paint (4L) and primer',               'INV-2024-006', NOW() - INTERVAL 8 DAY, 1),
 (11, NULL, 14,   9,    4, 'CHARGE', 4500.00, 'LABOR',    'Hardwood floor sanding and repair labour',      'INV-2024-007', NOW() - INTERVAL 12 DAY, 1),
 (12, NULL, 14,   9,    4, 'CHARGE', 1800.00, 'MATERIAL', 'Wood filler and polyurethane finish sealant',   'INV-2024-007', NOW() - INTERVAL 12 DAY, 1),
 (13, 1,    1,    NULL, 1, 'CHARGE', 2800.00, 'MATERIAL', 'High-pressure brass stopcock and PVC pipe',    'INV-2024-008', NOW() - INTERVAL 80 MINUTE, 1),
 (14, 15,   NULL, NULL, 1, 'CHARGE', 1900.00, 'LABOR',    'Geyser thermostat reset and element check',     'INV-2024-009', NOW() - INTERVAL 14 DAY, 1),
 (15, 16,   NULL, NULL, 2, 'CHARGE', 2100.00, 'LABOR',    'Kitchen exhaust motor capacitor replacement',   'INV-2024-010', NOW() - INTERVAL 11 DAY, 1),
 (16, 17,   NULL, NULL, 1, 'CHARGE', 5200.00, 'MATERIAL', 'Booster pressure regulator valve',             'INV-2024-011', NOW() - INTERVAL 18 DAY, 1),
 (17, 18,   NULL, NULL, 4, 'CHARGE', 1200.00, 'LABOR',    'Sliding wardrobe track realignment',            'INV-2024-012', NOW() - INTERVAL 17 DAY, 1),
 (18, 20,   NULL, NULL, 3, 'CHARGE', 1600.00, 'LABOR',    'AC thermostat PCB replacement',                 'INV-2024-013', NOW() - INTERVAL 9 DAY, 1);

-- deposit disputes
INSERT INTO deposit_disputes (dispute_id, lease_id, tenant_id, deposit_held, status, opened_at, resolved_at) VALUES
 (1, 2,  2,  44000.00, 'RESOLVED',              NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 2 DAY),
 (2, 3,  3,  64000.00, 'AWAITING_HUMAN_REVIEW', NOW() - INTERVAL 7 DAY,  NULL),
 (3, 6,  6,  60000.00, 'RESOLVED',              NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 5 DAY),
 (4, 8,  8,  54000.00, 'AWAITING_HUMAN_REVIEW', NOW() - INTERVAL 5 DAY,  NULL),
 (5, 11, 11, 62000.00, 'OPEN',                  NOW() - INTERVAL 2 DAY,  NULL);

-- dispute items
INSERT INTO dispute_items (dispute_item_id, dispute_id, move_out_item_id, move_in_item_id, claimed_amount, tenant_position, tenant_comment,
                           system_classification, applied_rule_id, rule_trace, operator_decision, decided_amount, decided_by_user_id, decided_at, decision_notes) VALUES
 (1, 1, 3,  1,    6000.00, 'CONTESTED', 'Marker marks are on one small patch and repair cost shouldn\'t exceed contractor bill.',
    'DAMAGE', 5, '{"rule_id":5,"rule_name":"Wall paint damaged by tenant action","matched":["item_category","move_out_condition","cause"],"baseline_found":true,"move_out_photos":2}',
    'CHARGE_REDUCED', 4600.00, 1, NOW() - INTERVAL 2 DAY,
    'Reduced from 6000 to match actual painter invoices INV-2024-006 (total 4600). Move-in photo verified clean base.'),

 (2, 1, 4,  2,   12000.00, 'CONTESTED', 'The carpet was already over 8 years old when I moved in.',
    'WEAR_AND_TEAR', 1, '{"rule_id":1,"rule_name":"Carpet past useful life, poor condition","matched":["item_category","min_age_years","move_out_condition"],"baseline_found":true,"move_out_photos":1}',
    'CHARGE_WAIVED', 0.00, 1, NOW() - INTERVAL 2 DAY,
    'Carpet is 9 years old; falls under normal wear-and-tear past useful life. Full deduction waived.'),

 (3, 2, 6,  NULL, 8500.00, 'CONTESTED', 'The countertop chip was present when I moved in, no baseline was recorded.',
    'NEEDS_HUMAN_REVIEW', NULL, '{"reason_code":"MISSING_MOVE_IN_BASELINE","baseline_found":false,"cause_noted":"UNKNOWN","move_out_photos":1}',
    NULL, NULL, NULL, NULL, NULL),

 (4, 3, 9,  7,    9000.00, 'CONTESTED', 'Scratches on floor were partly from old movers, but I acknowledge some.',
    'DAMAGE', 6, '{"rule_id":6,"rule_name":"Flooring damaged by tenant action","matched":["item_category","move_out_condition","cause"],"baseline_found":true,"move_out_photos":2}',
    'CHARGE_REDUCED', 6300.00, 2, NOW() - INTERVAL 5 DAY,
    'Reconciled against invoice INV-2024-007 (total 6300). Deduction reduced from 9000 to 6300.'),

 (5, 3, 10, 8,    11000.00, 'ACCEPTED', 'Acknowledged bedroom carpet condition.',
    'WEAR_AND_TEAR', 1, '{"rule_id":1,"rule_name":"Carpet past useful life, poor condition","matched":["item_category","min_age_years","move_out_condition"],"baseline_found":true,"move_out_photos":1}',
    'CHARGE_WAIVED', 0.00, 2, NOW() - INTERVAL 5 DAY,
    'Auto-classified as wear and tear: carpet is 10 years old. Charge waived completely for tenant.'),

 (6, 4, 13, 11,   4500.00, 'CONTESTED', 'Mirror cracked due to settling cracks in wall, not deliberate impact.',
    'DAMAGE', 8, '{"rule_id":8,"rule_name":"Fixture damaged by tenant action","matched":["item_category","move_out_condition","cause"],"baseline_found":true,"move_out_photos":1}',
    NULL, NULL, NULL, NULL, NULL),

 (7, 4, 14, 12,   7000.00, 'CONTESTED', 'Microwave was over 11 years old and simply stopped functioning.',
    'WEAR_AND_TEAR', 7, '{"rule_id":7,"rule_name":"Appliance past useful life, poor condition","matched":["item_category","min_age_years","move_out_condition"],"baseline_found":true,"move_out_photos":1}',
    'CHARGE_WAIVED', 0.00, 1, NOW() - INTERVAL 1 DAY,
    'Appliance 12 years old, exceeded 10-year useful life depreciation. Full waiver applied.'),

 (8, 5, 17, 15,   3500.00, 'CONTESTED', 'Dining room paint marks are natural furniture rub wear.',
    'WEAR_AND_TEAR', 3, '{"rule_id":3,"rule_name":"Wall paint faded with age","matched":["item_category","min_age_years","move_out_condition"],"baseline_found":true,"move_out_photos":1}',
    NULL, NULL, NULL, NULL, NULL),

 (9, 5, 18, 16,   2200.00, 'CONTESTED', 'Cabinet latch was loose throughout tenure.',
    'DAMAGE', 8, '{"rule_id":8,"rule_name":"Fixture damaged by tenant action","matched":["item_category","move_out_condition","cause"],"baseline_found":true,"move_out_photos":1}',
    NULL, NULL, NULL, NULL, NULL),

 (10, 1, 20, 19,  2500.00, 'CONTESTED', 'Balcony lock was flimsy metal.',
    'DAMAGE', 8, '{"rule_id":8,"rule_name":"Fixture damaged by tenant action","matched":["item_category","move_out_condition","cause"],"baseline_found":true,"move_out_photos":1}',
    'CHARGE_APPLIED', 2500.00, 1, NOW() - INTERVAL 2 DAY,
    'Move-in baseline showed new lock; snapped lever is mechanical abuse. Full charge confirmed.');

-- evidence files
INSERT INTO evidence_files (evidence_id, inspection_item_id, issue_id, dispute_item_id, s3_key, file_name, content_type, size_bytes, sha256, captured_at, uploaded_by_user_id) VALUES
 (1,  1,    NULL, NULL, 'evidence/lease-2/move-in/living-room-wall.jpg',   'living-room-wall.jpg',   'image/jpeg', 2481920, REPEAT('a',64), '2024-03-01 10:15:00', 1),
 (2,  3,    NULL, 1,    'evidence/lease-2/move-out/living-room-wall.jpg',  'living-room-wall.jpg',   'image/jpeg', 2650112, REPEAT('b',64), NOW() - INTERVAL 12 DAY, 1),
 (3,  2,    NULL, NULL, 'evidence/lease-2/move-in/bedroom-carpet.jpg',     'bedroom-carpet.jpg',     'image/jpeg', 2210048, REPEAT('c',64), '2024-03-01 10:25:00', 1),
 (4,  4,    NULL, 2,    'evidence/lease-2/move-out/bedroom-carpet.jpg',    'bedroom-carpet.jpg',     'image/jpeg', 2399232, REPEAT('d',64), NOW() - INTERVAL 12 DAY, 1),
 (5,  NULL, 1,    NULL, 'evidence/issues/1/kitchen-sink-burst.jpg',        'kitchen-sink-burst.jpg', 'image/jpeg', 1875456, REPEAT('e',64), NOW() - INTERVAL 105 MINUTE, 3),
 (6,  6,    NULL, 3,    'evidence/lease-3/move-out/kitchen-countertop.jpg','kitchen-countertop.jpg', 'image/jpeg', 2012160, REPEAT('f',64), NOW() - INTERVAL 25 DAY, 1),
 (7,  7,    NULL, NULL, 'evidence/lease-6/move-in/living-hardwood.jpg',    'living-hardwood.jpg',    'image/jpeg', 2560000, REPEAT('1',64), '2024-02-01 11:00:00', 2),
 (8,  9,    NULL, 4,    'evidence/lease-6/move-out/living-hardwood.jpg',   'living-hardwood.jpg',    'image/jpeg', 2740000, REPEAT('2',64), NOW() - INTERVAL 18 DAY, 2),
 (9,  11,   NULL, NULL, 'evidence/lease-8/move-in/bathroom-mirror.jpg',    'bathroom-mirror.jpg',    'image/jpeg', 1980000, REPEAT('3',64), '2024-01-01 14:30:00', 1),
 (10, 13,   NULL, 6,    'evidence/lease-8/move-out/bathroom-mirror.jpg',   'bathroom-mirror.jpg',    'image/jpeg', 2150000, REPEAT('4',64), NOW() - INTERVAL 8 DAY,  1),
 (11, NULL, 6,    NULL, 'evidence/issues/6/sparking-outlet.jpg',           'sparking-outlet.jpg',    'image/jpeg', 1650000, REPEAT('5',64), NOW() - INTERVAL 3 DAY,  7),
 (12, 19,   NULL, 10,   'evidence/lease-2/move-out/balcony-lock.jpg',      'balcony-lock.jpg',       'image/jpeg', 1890000, REPEAT('6',64), NOW() - INTERVAL 12 DAY, 1);

-- review queue
INSERT INTO review_queue (dispute_item_id, reason_code, reason_text, status) VALUES
 (3, 'MISSING_MOVE_IN_BASELINE', 'No move-in baseline inspection record for kitchen quartz slab; cause is UNKNOWN.', 'PENDING'),
 (6, 'TENANT_EVIDENCE_CONTEST',  'Tenant claims structural wall settling caused mirror fracture rather than impact.', 'IN_REVIEW'),
 (8, 'MARGINAL_AGE_WEAR',        'Dining room paint is 3 years old; evaluating normal wear versus tenant scuffs.',   'PENDING');

-- dispute events
INSERT INTO dispute_events (dispute_id, dispute_item_id, event_type, actor_type, actor_user_id, detail, created_at) VALUES
 (1, NULL, 'DISPUTE_OPENED',        'ADMIN',  1,    'Move-out deductions raised for 3 items',          NOW() - INTERVAL 10 DAY),
 (1, 1,    'ITEM_CLASSIFIED',       'SYSTEM', NULL, 'Classified DAMAGE by rule 5',                     NOW() - INTERVAL 10 DAY),
 (1, 2,    'ITEM_CLASSIFIED',       'SYSTEM', NULL, 'Classified WEAR_AND_TEAR by rule 1',              NOW() - INTERVAL 10 DAY),
 (1, 10,   'ITEM_CLASSIFIED',       'SYSTEM', NULL, 'Classified DAMAGE by rule 8',                     NOW() - INTERVAL 10 DAY),
 (1, 1,    'TENANT_CONTESTED',      'TENANT', 4,    'Tenant contested claimed wall paint repair cost', NOW() - INTERVAL 9 DAY),
 (1, 2,    'TENANT_CONTESTED',      'TENANT', 4,    'Tenant highlighted 8-year age of bedroom carpet', NOW() - INTERVAL 9 DAY),
 (1, 10,   'TENANT_CONTESTED',      'TENANT', 4,    'Tenant contested lock repair deduction',          NOW() - INTERVAL 9 DAY),
 (1, 1,    'OPERATOR_DECISION',     'ADMIN',  1,    'CHARGE_REDUCED to 4600 per actual contractor bill',NOW() - INTERVAL 2 DAY),
 (1, 2,    'OPERATOR_DECISION',     'ADMIN',  1,    'CHARGE_WAIVED: wear and tear verified',           NOW() - INTERVAL 2 DAY),
 (1, 10,   'OPERATOR_DECISION',     'ADMIN',  1,    'CHARGE_APPLIED: verified broken hardware',        NOW() - INTERVAL 2 DAY),
 (1, NULL, 'DISPUTE_RESOLVED',      'ADMIN',  1,    'All dispute items adjudicated and ledger closed', NOW() - INTERVAL 2 DAY),
 (2, NULL, 'DISPUTE_OPENED',        'ADMIN',  1,    'Move-out claim raised for kitchen countertop',    NOW() - INTERVAL 7 DAY),
 (2, 3,    'ROUTED_TO_HUMAN_REVIEW','SYSTEM', NULL, 'MISSING_MOVE_IN_BASELINE',                        NOW() - INTERVAL 7 DAY),
 (3, NULL, 'DISPUTE_OPENED',        'ADMIN',  2,    'Move-out deductions filed for floor and carpet',  NOW() - INTERVAL 14 DAY),
 (3, 4,    'ITEM_CLASSIFIED',       'SYSTEM', NULL, 'Classified DAMAGE by rule 6',                     NOW() - INTERVAL 14 DAY),
 (3, 5,    'ITEM_CLASSIFIED',       'SYSTEM', NULL, 'Classified WEAR_AND_TEAR by rule 1',              NOW() - INTERVAL 14 DAY),
 (3, 4,    'OPERATOR_DECISION',     'ADMIN',  2,    'CHARGE_REDUCED to 6300 per invoice',              NOW() - INTERVAL 5 DAY),
 (3, 5,    'OPERATOR_DECISION',     'ADMIN',  2,    'CHARGE_WAIVED completely',                        NOW() - INTERVAL 5 DAY),
 (3, NULL, 'DISPUTE_RESOLVED',      'ADMIN',  2,    'Dispute settled',                                 NOW() - INTERVAL 5 DAY);

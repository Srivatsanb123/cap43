USE cap43;

INSERT INTO severity_rules (severity, rank_order, sla_hours, escalate_l1_pct, escalate_l2_pct, description) VALUES
 ('EMERGENCY', 1,   4, 50, 100, 'Risk to safety or property: active leak/flooding, gas smell, sparking/electrical hazard, smoke, sewage backup'),
 ('HIGH',      2,  24, 50, 100, 'Loss of essential service: no water, no power, lock failure'),
 ('MEDIUM',    3,  72, 50, 100, 'Functional problem, habitable: door/window faults, cracks'),
 ('LOW',       4, 168, 50, 100, 'Cosmetic or minor: paint, light bulb, squeaks');

INSERT INTO issue_keywords (keyword, severity) VALUES
 ('leak','EMERGENCY'), ('flood','EMERGENCY'), ('burst','EMERGENCY'), ('gas','EMERGENCY'),
 ('sparking','EMERGENCY'), ('smoke','EMERGENCY'), ('sewage','EMERGENCY'),
 ('no water','HIGH'), ('no power','HIGH'), ('lock','HIGH'),
 ('door','MEDIUM'), ('window','MEDIUM'), ('crack','MEDIUM'),
 ('paint','LOW'), ('light bulb','LOW'), ('squeak','LOW');

-- rule_id order matters for the demo seed (1..8)
INSERT INTO wear_damage_rules
 (rule_name, item_category, min_age_years, max_age_years, move_in_condition, move_out_condition, cause, classification, priority, notes) VALUES
 ('Carpet past useful life, poor condition',        'CARPET',     8,    NULL, NULL, 'POOR',    NULL,            'WEAR_AND_TEAR', 10, 'Carpet 8+ years old in poor condition is expected ageing'),
 ('Carpet damaged by tenant action',                'CARPET',     NULL, NULL, NULL, 'DAMAGED', 'TENANT_ACTION', 'DAMAGE',        10, NULL),
 ('Wall paint faded with age',                      'WALL_PAINT', 3,    NULL, NULL, 'FAIR',    NULL,            'WEAR_AND_TEAR', 10, NULL),
 ('Wall paint worn, normal use',                    'WALL_PAINT', 3,    NULL, NULL, 'POOR',    'NORMAL_USE',    'WEAR_AND_TEAR', 10, NULL),
 ('Wall paint damaged by tenant action',            'WALL_PAINT', NULL, NULL, NULL, 'DAMAGED', 'TENANT_ACTION', 'DAMAGE',        10, 'Marker stains, holes beyond normal fixtures'),
 ('Flooring damaged by tenant action',              'FLOORING',   NULL, NULL, NULL, 'DAMAGED', 'TENANT_ACTION', 'DAMAGE',        10, NULL),
 ('Appliance past useful life, poor condition',     'APPLIANCE',  10,   NULL, NULL, 'POOR',    NULL,            'WEAR_AND_TEAR', 10, NULL),
 ('Fixture damaged by tenant action',               'FIXTURE',    NULL, NULL, NULL, 'DAMAGED', 'TENANT_ACTION', 'DAMAGE',        10, NULL);

INSERT INTO system_config (config_key, config_value, description) VALUES
 ('dispute.require_move_in_baseline', 'true', 'No move-in record for an item -> route to human review'),
 ('dispute.min_move_out_photos',      '1',    'Minimum move-out photos per disputed item before auto-classification'),
 ('dispute.cost_variance_pct',        '50',   'Claimed amount above ledger cost by more than this % -> human review'),
 ('pii.mask_by_default',              'true', 'Admin views mask email/phone unless unmasked'),
 ('pii.unmask_requires_reason',       'true', 'Unmasking PII needs a reason, logged in audit_log');

INSERT INTO system_config (config_key, config_value, description) VALUES
 ('emergency.ack_minutes', '15', 'EMERGENCY issue not acknowledged by a vendor within this many minutes -> escalation level 1');

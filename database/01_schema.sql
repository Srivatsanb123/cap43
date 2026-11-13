USE cap43;

-- users
CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(10) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_users_role CHECK (role IN ('ADMIN','TENANT','VENDOR'))
);

-- properties, units, tenants, leases, and vendors
CREATE TABLE properties (
  property_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address_line VARCHAR(200),
  city VARCHAR(60),
  state VARCHAR(60),
  postal_code VARCHAR(12),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE units (
  unit_id INT AUTO_INCREMENT PRIMARY KEY,
  property_id INT NOT NULL,
  unit_number VARCHAR(20) NOT NULL,
  floor_no INT,
  bedrooms INT,
  UNIQUE KEY uq_unit (property_id, unit_number),
  FOREIGN KEY (property_id) REFERENCES properties(property_id)
);

CREATE TABLE tenants (
  tenant_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  tenant_ref VARCHAR(12) NOT NULL UNIQUE,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE leases (
  lease_id INT AUTO_INCREMENT PRIMARY KEY,
  unit_id INT NOT NULL,
  tenant_id INT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  monthly_rent DECIMAL(10,2) NOT NULL,
  deposit_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
  move_out_date DATE NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (unit_id) REFERENCES units(unit_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
  CONSTRAINT chk_lease_status CHECK (status IN ('ACTIVE','ENDED','TERMINATED'))
);

CREATE TABLE vendors (
  vendor_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  company_name VARCHAR(100) NOT NULL,
  trade VARCHAR(40) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- rules
CREATE TABLE severity_rules (
  severity VARCHAR(20) PRIMARY KEY,
  rank_order INT NOT NULL,
  sla_hours INT NOT NULL,
  escalate_l1_pct INT NOT NULL DEFAULT 50,
  escalate_l2_pct INT NOT NULL DEFAULT 100,
  description VARCHAR(255)
);

CREATE TABLE issue_keywords (
  keyword_id INT AUTO_INCREMENT PRIMARY KEY,
  keyword VARCHAR(50) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  FOREIGN KEY (severity) REFERENCES severity_rules(severity)
);

CREATE TABLE wear_damage_rules (
  rule_id INT AUTO_INCREMENT PRIMARY KEY,
  rule_name VARCHAR(120) NOT NULL,
  item_category VARCHAR(30) NOT NULL,
  min_age_years INT NULL,
  max_age_years INT NULL,
  move_in_condition VARCHAR(10) NULL,
  move_out_condition VARCHAR(10) NULL,
  cause VARCHAR(15) NULL,
  classification VARCHAR(20) NOT NULL,
  priority INT NOT NULL DEFAULT 100,
  active TINYINT(1) NOT NULL DEFAULT 1,
  notes VARCHAR(255) NULL,
  CONSTRAINT chk_rule_class CHECK (classification IN ('WEAR_AND_TEAR','DAMAGE'))
);

CREATE TABLE system_config (
  config_key VARCHAR(60) PRIMARY KEY,
  config_value VARCHAR(100) NOT NULL,
  description VARCHAR(255)
);

-- inspections
CREATE TABLE inspections (
  inspection_id INT AUTO_INCREMENT PRIMARY KEY,
  lease_id INT NOT NULL,
  inspection_type VARCHAR(10) NOT NULL,
  inspected_on DATE NOT NULL,
  inspector_user_id INT NULL,
  notes TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lease_id) REFERENCES leases(lease_id),
  FOREIGN KEY (inspector_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_insp_type CHECK (inspection_type IN ('MOVE_IN','MOVE_OUT'))
);

CREATE TABLE inspection_items (
  item_id INT AUTO_INCREMENT PRIMARY KEY,
  inspection_id INT NOT NULL,
  item_category VARCHAR(30) NOT NULL,
  item_name VARCHAR(80) NOT NULL,
  location VARCHAR(60) NULL,
  condition_rating VARCHAR(10) NOT NULL,
  item_age_years INT NULL,
  cause_noted VARCHAR(15) NULL,
  remarks VARCHAR(500) NULL,
  FOREIGN KEY (inspection_id) REFERENCES inspections(inspection_id),
  CONSTRAINT chk_item_condition CHECK (condition_rating IN ('NEW','GOOD','FAIR','POOR','DAMAGED','MISSING')),
  CONSTRAINT chk_item_cause CHECK (cause_noted IS NULL OR cause_noted IN ('NORMAL_USE','TENANT_ACTION','UNKNOWN'))
);

-- maintenance issues and work orders
CREATE TABLE issues (
  issue_id INT AUTO_INCREMENT PRIMARY KEY,
  unit_id INT NOT NULL,
  tenant_id INT NOT NULL,
  category VARCHAR(30) NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT NOT NULL,
  severity VARCHAR(20) NOT NULL,
  severity_source VARCHAR(10) NOT NULL DEFAULT 'AUTO',
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  escalation_level INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  acknowledged_at DATETIME NULL,
  sla_due_at DATETIME NOT NULL,
  resolved_at DATETIME NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (unit_id) REFERENCES units(unit_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
  FOREIGN KEY (severity) REFERENCES severity_rules(severity),
  CONSTRAINT chk_issue_status CHECK (status IN ('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','CANCELLED')),
  CONSTRAINT chk_issue_source CHECK (severity_source IN ('AUTO','MANUAL'))
);

CREATE TABLE issue_status_history (
  history_id INT AUTO_INCREMENT PRIMARY KEY,
  issue_id INT NOT NULL,
  old_status VARCHAR(20) NULL,
  new_status VARCHAR(20) NOT NULL,
  old_severity VARCHAR(20) NULL,
  new_severity VARCHAR(20) NOT NULL,
  changed_by_user_id INT NULL,
  change_reason VARCHAR(255) NULL,
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
  FOREIGN KEY (changed_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE work_orders (
  work_order_id INT AUTO_INCREMENT PRIMARY KEY,
  issue_id INT NULL,
  inspection_item_id INT NULL,
  vendor_id INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
  assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  assigned_by_user_id INT NOT NULL,
  accepted_at DATETIME NULL,
  started_at DATETIME NULL,
  completed_at DATETIME NULL,
  vendor_notes TEXT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
  FOREIGN KEY (inspection_item_id) REFERENCES inspection_items(item_id),
  FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id),
  FOREIGN KEY (assigned_by_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_wo_status CHECK (status IN ('ASSIGNED','ACCEPTED','IN_PROGRESS','ON_HOLD','COMPLETED','CANCELLED')),
  CONSTRAINT chk_wo_target CHECK ((issue_id IS NOT NULL) OR (inspection_item_id IS NOT NULL))
);

CREATE TABLE work_order_updates (
  update_id INT AUTO_INCREMENT PRIMARY KEY,
  work_order_id INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  note TEXT NULL,
  created_by_user_id INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (work_order_id) REFERENCES work_orders(work_order_id),
  FOREIGN KEY (created_by_user_id) REFERENCES users(user_id)
);

CREATE TABLE escalations (
  escalation_id INT AUTO_INCREMENT PRIMARY KEY,
  issue_id INT NOT NULL,
  level INT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  triggered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  acknowledged_by_user_id INT NULL,
  acknowledged_at DATETIME NULL,
  UNIQUE KEY uq_issue_level (issue_id, level),
  FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
  FOREIGN KEY (acknowledged_by_user_id) REFERENCES users(user_id)
);

-- repair cost ledger
CREATE TABLE repair_costs (
  cost_id INT AUTO_INCREMENT PRIMARY KEY,
  issue_id INT NULL,
  work_order_id INT NULL,
  inspection_item_id INT NULL,
  vendor_id INT NULL,
  entry_type VARCHAR(10) NOT NULL DEFAULT 'CHARGE',
  amount DECIMAL(10,2) NOT NULL,
  cost_type VARCHAR(10) NOT NULL,
  description VARCHAR(255) NULL,
  invoice_ref VARCHAR(40) NULL,
  reverses_cost_id INT NULL,
  incurred_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  entered_by_user_id INT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
  FOREIGN KEY (work_order_id) REFERENCES work_orders(work_order_id),
  FOREIGN KEY (inspection_item_id) REFERENCES inspection_items(item_id),
  FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id),
  FOREIGN KEY (reverses_cost_id) REFERENCES repair_costs(cost_id),
  FOREIGN KEY (entered_by_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_cost_type CHECK (cost_type IN ('LABOR','MATERIAL','OTHER')),
  CONSTRAINT chk_cost_entry CHECK (entry_type IN ('CHARGE','ADJUSTMENT')),
  CONSTRAINT chk_cost_link CHECK (issue_id IS NOT NULL OR work_order_id IS NOT NULL OR inspection_item_id IS NOT NULL)
);

-- deposit disputes
CREATE TABLE deposit_disputes (
  dispute_id INT AUTO_INCREMENT PRIMARY KEY,
  lease_id INT NOT NULL,
  tenant_id INT NOT NULL,
  deposit_held DECIMAL(10,2) NOT NULL,
  status VARCHAR(25) NOT NULL DEFAULT 'OPEN',
  opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at DATETIME NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (lease_id) REFERENCES leases(lease_id),
  FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
  CONSTRAINT chk_disp_status CHECK (status IN ('OPEN','AWAITING_HUMAN_REVIEW','RESOLVED'))
);

CREATE TABLE dispute_items (
  dispute_item_id INT AUTO_INCREMENT PRIMARY KEY,
  dispute_id INT NOT NULL,
  move_out_item_id INT NOT NULL,
  move_in_item_id INT NULL,
  claimed_amount DECIMAL(10,2) NOT NULL,
  tenant_position VARCHAR(15) NOT NULL DEFAULT 'NO_RESPONSE',
  tenant_comment VARCHAR(1000) NULL,
  system_classification VARCHAR(25) NULL,
  applied_rule_id INT NULL,
  rule_trace JSON NULL,
  operator_decision VARCHAR(20) NULL,
  decided_amount DECIMAL(10,2) NULL,
  decided_by_user_id INT NULL,
  decided_at DATETIME NULL,
  decision_notes VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (dispute_id) REFERENCES deposit_disputes(dispute_id),
  FOREIGN KEY (move_out_item_id) REFERENCES inspection_items(item_id),
  FOREIGN KEY (move_in_item_id) REFERENCES inspection_items(item_id),
  FOREIGN KEY (applied_rule_id) REFERENCES wear_damage_rules(rule_id),
  FOREIGN KEY (decided_by_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_di_position CHECK (tenant_position IN ('ACCEPTED','CONTESTED','NO_RESPONSE')),
  CONSTRAINT chk_di_class CHECK (system_classification IS NULL OR system_classification IN ('WEAR_AND_TEAR','DAMAGE','NEEDS_HUMAN_REVIEW')),
  CONSTRAINT chk_di_decision CHECK (operator_decision IS NULL OR operator_decision IN ('CHARGE_APPLIED','CHARGE_REDUCED','CHARGE_WAIVED'))
);

-- evidence files
CREATE TABLE evidence_files (
  evidence_id INT AUTO_INCREMENT PRIMARY KEY,
  inspection_item_id INT NULL,
  issue_id INT NULL,
  dispute_item_id INT NULL,
  s3_key VARCHAR(255) NOT NULL,
  file_name VARCHAR(150) NOT NULL,
  content_type VARCHAR(60) NOT NULL,
  size_bytes BIGINT NOT NULL,
  sha256 CHAR(64) NOT NULL,
  captured_at DATETIME NULL,
  uploaded_by_user_id INT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (inspection_item_id) REFERENCES inspection_items(item_id),
  FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
  FOREIGN KEY (dispute_item_id) REFERENCES dispute_items(dispute_item_id),
  FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_evidence_link CHECK (inspection_item_id IS NOT NULL OR issue_id IS NOT NULL OR dispute_item_id IS NOT NULL)
);

-- review queue
CREATE TABLE review_queue (
  review_id INT AUTO_INCREMENT PRIMARY KEY,
  dispute_item_id INT NULL,
  issue_id INT NULL,
  reason_code VARCHAR(40) NOT NULL,
  reason_text VARCHAR(500) NOT NULL,
  status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
  assigned_to_user_id INT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  decided_at DATETIME NULL,
  decided_by_user_id INT NULL,
  outcome_notes VARCHAR(1000) NULL,
  FOREIGN KEY (dispute_item_id) REFERENCES dispute_items(dispute_item_id),
  FOREIGN KEY (issue_id) REFERENCES issues(issue_id),
  FOREIGN KEY (assigned_to_user_id) REFERENCES users(user_id),
  FOREIGN KEY (decided_by_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_review_status CHECK (status IN ('PENDING','IN_REVIEW','DECIDED')),
  CONSTRAINT chk_review_target CHECK ((dispute_item_id IS NOT NULL) + (issue_id IS NOT NULL) = 1)
);

-- dispute events and audit log
CREATE TABLE dispute_events (
  event_id INT AUTO_INCREMENT PRIMARY KEY,
  dispute_id INT NOT NULL,
  dispute_item_id INT NULL,
  event_type VARCHAR(30) NOT NULL,
  actor_type VARCHAR(10) NOT NULL,
  actor_user_id INT NULL,
  detail VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (dispute_id) REFERENCES deposit_disputes(dispute_id),
  FOREIGN KEY (dispute_item_id) REFERENCES dispute_items(dispute_item_id),
  FOREIGN KEY (actor_user_id) REFERENCES users(user_id),
  CONSTRAINT chk_event_actor CHECK (actor_type IN ('TENANT','ADMIN','SYSTEM'))
);

CREATE TABLE audit_log (
  audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NULL,
  action VARCHAR(40) NOT NULL,
  entity_type VARCHAR(40) NULL,
  entity_id VARCHAR(40) NULL,
  detail VARCHAR(500) NULL,
  ip_address VARCHAR(45) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_user (user_id),
  INDEX idx_audit_action (action)
);

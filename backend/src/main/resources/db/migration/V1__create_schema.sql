CREATE OR REPLACE FUNCTION solarflow_set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

CREATE TABLE companies (
  id UUID PRIMARY KEY,
  name VARCHAR(160) NOT NULL,
  legal_name VARCHAR(200),
  slug VARCHAR(80) NOT NULL UNIQUE,
  gstin VARCHAR(15),
  email VARCHAR(160),
  phone VARCHAR(20),
  address_line VARCHAR(240),
  city VARCHAR(80),
  state VARCHAR(80),
  pincode VARCHAR(10),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_users_id_company UNIQUE (id, company_id),
  CONSTRAINT users_email_lower CHECK (email = lower(email)),
  CONSTRAINT users_role_chk CHECK (role IN ('OWNER', 'ADMIN', 'SALES', 'OPERATIONS', 'TECHNICIAN')),
  CONSTRAINT users_status_chk CHECK (status IN ('ACTIVE', 'INVITED', 'DISABLED'))
);

CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  company_id UUID NOT NULL,
  family_id UUID NOT NULL,
  token_hash VARCHAR(128) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  replaced_by_id UUID,
  user_agent VARCHAR(255),
  ip_address VARCHAR(64),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_refresh_user_company FOREIGN KEY (user_id, company_id) REFERENCES users (id, company_id)
);

CREATE TABLE customers (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  display_name VARCHAR(160) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(160),
  address_line VARCHAR(240),
  city VARCHAR(80),
  state VARCHAR(80),
  pincode VARCHAR(10),
  status VARCHAR(32) NOT NULL,
  source VARCHAR(80),
  notes VARCHAR(2000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_customers_id_company UNIQUE (id, company_id),
  CONSTRAINT customers_status_chk CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE leads (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  customer_id UUID,
  contact_name VARCHAR(160) NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(160),
  city VARCHAR(80),
  source VARCHAR(80),
  status VARCHAR(32) NOT NULL,
  priority VARCHAR(32) NOT NULL,
  assigned_to UUID,
  follow_up_on DATE,
  estimated_capacity_kw NUMERIC(8, 2),
  notes VARCHAR(2000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_leads_id_company UNIQUE (id, company_id),
  CONSTRAINT fk_leads_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_leads_assignee FOREIGN KEY (assigned_to, company_id) REFERENCES users (id, company_id),
  CONSTRAINT leads_status_chk CHECK (status IN ('NEW', 'CONTACTED', 'SURVEY_SCHEDULED', 'QUOTED', 'NEGOTIATION', 'WON', 'LOST')),
  CONSTRAINT leads_priority_chk CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE TABLE site_surveys (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  lead_id UUID NOT NULL,
  customer_id UUID,
  status VARCHAR(32) NOT NULL,
  scheduled_on DATE,
  surveyed_by UUID,
  roof_type VARCHAR(80),
  roof_area_sqft NUMERIC(10, 2),
  orientation VARCHAR(32),
  shading_notes VARCHAR(500),
  monthly_bill NUMERIC(12, 2),
  sanctioned_load_kw NUMERIC(8, 2),
  connection_type VARCHAR(32),
  latitude NUMERIC(9, 6),
  longitude NUMERIC(9, 6),
  notes VARCHAR(2000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_surveys_id_company UNIQUE (id, company_id),
  CONSTRAINT fk_surveys_lead FOREIGN KEY (lead_id, company_id) REFERENCES leads (id, company_id),
  CONSTRAINT fk_surveys_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_surveys_surveyor FOREIGN KEY (surveyed_by, company_id) REFERENCES users (id, company_id),
  CONSTRAINT surveys_status_chk CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE survey_photos (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  survey_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  storage_key VARCHAR(255) NOT NULL,
  content_type VARCHAR(120) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_survey_photos FOREIGN KEY (survey_id, company_id) REFERENCES site_surveys (id, company_id) ON DELETE CASCADE
);

CREATE TABLE products (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  sku VARCHAR(64) NOT NULL,
  name VARCHAR(160) NOT NULL,
  category VARCHAR(32) NOT NULL,
  brand VARCHAR(80),
  model VARCHAR(80),
  unit VARCHAR(24) NOT NULL,
  unit_price NUMERIC(14, 2) NOT NULL,
  wattage INTEGER,
  warranty_years INTEGER,
  stock_quantity NUMERIC(12, 2) NOT NULL DEFAULT 0,
  reorder_level NUMERIC(12, 2) NOT NULL DEFAULT 0,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_products_id_company UNIQUE (id, company_id),
  CONSTRAINT uq_products_company_sku UNIQUE (company_id, sku),
  CONSTRAINT products_category_chk CHECK (category IN ('PANEL', 'INVERTER', 'STRUCTURE', 'CABLE', 'ELECTRICAL', 'OTHER')),
  CONSTRAINT products_price_chk CHECK (unit_price >= 0),
  CONSTRAINT products_stock_chk CHECK (stock_quantity >= 0 AND reorder_level >= 0)
);

CREATE TABLE stock_movements (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  product_id UUID NOT NULL,
  movement_type VARCHAR(32) NOT NULL,
  quantity NUMERIC(12, 2) NOT NULL,
  reference_type VARCHAR(32),
  reference_id UUID,
  notes VARCHAR(500),
  created_by UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_stock_product FOREIGN KEY (product_id, company_id) REFERENCES products (id, company_id),
  CONSTRAINT fk_stock_user FOREIGN KEY (created_by, company_id) REFERENCES users (id, company_id),
  CONSTRAINT stock_type_chk CHECK (movement_type IN ('IN', 'OUT', 'ADJUSTMENT', 'ALLOCATION')),
  CONSTRAINT stock_qty_chk CHECK (quantity <> 0)
);

CREATE TABLE quotations (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  number VARCHAR(32) NOT NULL,
  lead_id UUID,
  customer_id UUID,
  survey_id UUID,
  status VARCHAR(32) NOT NULL,
  system_capacity_kw NUMERIC(8, 2),
  panel_count INTEGER,
  subtotal NUMERIC(14, 2) NOT NULL DEFAULT 0,
  discount_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
  tax_rate NUMERIC(5, 2) NOT NULL DEFAULT 0,
  tax_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
  total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
  valid_until DATE,
  notes VARCHAR(2000),
  created_by UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_quotations_id_company UNIQUE (id, company_id),
  CONSTRAINT uq_quotations_company_number UNIQUE (company_id, number),
  CONSTRAINT fk_quotations_lead FOREIGN KEY (lead_id, company_id) REFERENCES leads (id, company_id),
  CONSTRAINT fk_quotations_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_quotations_survey FOREIGN KEY (survey_id, company_id) REFERENCES site_surveys (id, company_id),
  CONSTRAINT fk_quotations_author FOREIGN KEY (created_by, company_id) REFERENCES users (id, company_id),
  CONSTRAINT quotations_status_chk CHECK (status IN ('DRAFT', 'SENT', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
  CONSTRAINT quotations_money_chk CHECK (
    subtotal >= 0 AND discount_amount >= 0 AND tax_rate >= 0 AND tax_amount >= 0 AND total_amount >= 0
  )
);

CREATE TABLE quotation_lines (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  quotation_id UUID NOT NULL,
  product_id UUID,
  description VARCHAR(240) NOT NULL,
  quantity NUMERIC(12, 2) NOT NULL,
  unit_price NUMERIC(14, 2) NOT NULL,
  line_total NUMERIC(14, 2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_lines_quotation FOREIGN KEY (quotation_id, company_id) REFERENCES quotations (id, company_id) ON DELETE CASCADE,
  CONSTRAINT fk_lines_product FOREIGN KEY (product_id, company_id) REFERENCES products (id, company_id),
  CONSTRAINT lines_qty_chk CHECK (quantity > 0 AND unit_price >= 0 AND line_total >= 0)
);

CREATE TABLE projects (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  customer_id UUID NOT NULL,
  quotation_id UUID,
  name VARCHAR(160) NOT NULL,
  status VARCHAR(32) NOT NULL,
  capacity_kw NUMERIC(8, 2),
  site_address VARCHAR(240),
  start_date DATE,
  target_date DATE,
  completed_date DATE,
  lead_technician_id UUID,
  progress_percent INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_projects_id_company UNIQUE (id, company_id),
  CONSTRAINT fk_projects_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_projects_quotation FOREIGN KEY (quotation_id, company_id) REFERENCES quotations (id, company_id),
  CONSTRAINT fk_projects_technician FOREIGN KEY (lead_technician_id, company_id) REFERENCES users (id, company_id),
  CONSTRAINT projects_status_chk CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED')),
  CONSTRAINT projects_progress_chk CHECK (progress_percent BETWEEN 0 AND 100)
);

CREATE TABLE project_tasks (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  project_id UUID NOT NULL,
  title VARCHAR(160) NOT NULL,
  description VARCHAR(2000),
  status VARCHAR(32) NOT NULL,
  assigned_to UUID,
  due_on DATE,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_tasks_project FOREIGN KEY (project_id, company_id) REFERENCES projects (id, company_id) ON DELETE CASCADE,
  CONSTRAINT fk_tasks_assignee FOREIGN KEY (assigned_to, company_id) REFERENCES users (id, company_id),
  CONSTRAINT tasks_status_chk CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'))
);

CREATE TABLE project_photos (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  project_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  storage_key VARCHAR(255) NOT NULL,
  caption VARCHAR(240),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_project_photos FOREIGN KEY (project_id, company_id) REFERENCES projects (id, company_id) ON DELETE CASCADE
);

CREATE TABLE payments (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  customer_id UUID NOT NULL,
  project_id UUID,
  amount NUMERIC(14, 2) NOT NULL,
  method VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  reference_no VARCHAR(64),
  receipt_number VARCHAR(40),
  paid_at TIMESTAMPTZ,
  notes VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_payments_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_payments_project FOREIGN KEY (project_id, company_id) REFERENCES projects (id, company_id),
  CONSTRAINT payments_method_chk CHECK (method IN ('CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE', 'CARD')),
  CONSTRAINT payments_status_chk CHECK (status IN ('PENDING', 'RECEIVED', 'FAILED', 'REFUNDED')),
  CONSTRAINT payments_amount_chk CHECK (amount > 0)
);

CREATE TABLE warranties (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  customer_id UUID NOT NULL,
  project_id UUID,
  product_id UUID,
  warranty_type VARCHAR(32) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  status VARCHAR(32) NOT NULL,
  coverage_notes VARCHAR(2000),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_warranties_id_company UNIQUE (id, company_id),
  CONSTRAINT fk_warranties_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_warranties_project FOREIGN KEY (project_id, company_id) REFERENCES projects (id, company_id),
  CONSTRAINT fk_warranties_product FOREIGN KEY (product_id, company_id) REFERENCES products (id, company_id),
  CONSTRAINT warranties_type_chk CHECK (warranty_type IN ('PANEL', 'INVERTER', 'WORKMANSHIP', 'OTHER')),
  CONSTRAINT warranties_status_chk CHECK (status IN ('ACTIVE', 'EXPIRING', 'EXPIRED', 'VOID')),
  CONSTRAINT warranties_dates_chk CHECK (end_date >= start_date)
);

CREATE TABLE service_requests (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL REFERENCES companies (id),
  customer_id UUID NOT NULL,
  project_id UUID,
  warranty_id UUID,
  title VARCHAR(160) NOT NULL,
  description VARCHAR(2000),
  priority VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  assigned_to UUID,
  resolution_notes VARCHAR(2000),
  opened_at TIMESTAMPTZ NOT NULL,
  resolved_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_service_customer FOREIGN KEY (customer_id, company_id) REFERENCES customers (id, company_id),
  CONSTRAINT fk_service_project FOREIGN KEY (project_id, company_id) REFERENCES projects (id, company_id),
  CONSTRAINT fk_service_warranty FOREIGN KEY (warranty_id, company_id) REFERENCES warranties (id, company_id),
  CONSTRAINT fk_service_assignee FOREIGN KEY (assigned_to, company_id) REFERENCES users (id, company_id),
  CONSTRAINT service_priority_chk CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
  CONSTRAINT service_status_chk CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
);

CREATE TABLE notifications (
  id UUID PRIMARY KEY,
  company_id UUID NOT NULL,
  user_id UUID NOT NULL,
  title VARCHAR(160) NOT NULL,
  body VARCHAR(500) NOT NULL,
  category VARCHAR(32) NOT NULL,
  read_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id, company_id) REFERENCES users (id, company_id) ON DELETE CASCADE
);

CREATE TABLE quotation_counters (
  company_id UUID PRIMARY KEY REFERENCES companies (id),
  next_value BIGINT NOT NULL CHECK (next_value > 0)
);

CREATE INDEX idx_users_company ON users (company_id);
CREATE INDEX idx_refresh_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_user ON refresh_tokens (user_id);
CREATE INDEX idx_customers_company_name ON customers (company_id, display_name);
CREATE INDEX idx_leads_company_status ON leads (company_id, status);
CREATE INDEX idx_leads_company_follow_up ON leads (company_id, follow_up_on);
CREATE INDEX idx_surveys_company_status ON site_surveys (company_id, status);
CREATE INDEX idx_products_company_category ON products (company_id, category);
CREATE INDEX idx_stock_product ON stock_movements (product_id, created_at DESC);
CREATE INDEX idx_quotations_company_status ON quotations (company_id, status);
CREATE INDEX idx_projects_company_status ON projects (company_id, status);
CREATE INDEX idx_payments_company_status ON payments (company_id, status);
CREATE INDEX idx_payments_company_paid_at ON payments (company_id, paid_at);
CREATE INDEX idx_warranties_company_end ON warranties (company_id, end_date);
CREATE INDEX idx_service_company_status ON service_requests (company_id, status);
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);

CREATE TRIGGER trg_companies_updated BEFORE UPDATE ON companies
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_refresh_updated BEFORE UPDATE ON refresh_tokens
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_customers_updated BEFORE UPDATE ON customers
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_leads_updated BEFORE UPDATE ON leads
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_surveys_updated BEFORE UPDATE ON site_surveys
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_survey_photos_updated BEFORE UPDATE ON survey_photos
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_products_updated BEFORE UPDATE ON products
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_stock_updated BEFORE UPDATE ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_quotations_updated BEFORE UPDATE ON quotations
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_lines_updated BEFORE UPDATE ON quotation_lines
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_projects_updated BEFORE UPDATE ON projects
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_tasks_updated BEFORE UPDATE ON project_tasks
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_project_photos_updated BEFORE UPDATE ON project_photos
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_payments_updated BEFORE UPDATE ON payments
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_warranties_updated BEFORE UPDATE ON warranties
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_service_updated BEFORE UPDATE ON service_requests
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();
CREATE TRIGGER trg_notifications_updated BEFORE UPDATE ON notifications
  FOR EACH ROW EXECUTE FUNCTION solarflow_set_updated_at();

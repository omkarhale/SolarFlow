INSERT INTO companies (
  id, name, legal_name, slug, gstin, email, phone, address_line, city, state, pincode
) VALUES (
  '11111111-1111-4111-8111-111111111111',
  'Helios Rooftop Solar',
  'Helios Rooftop Solar Pvt Ltd',
  'helios',
  '27AABCH1234F1Z5',
  'hello@helios.example',
  '+91 20 4000 1200',
  '4th Floor, Baner Business Bay',
  'Pune',
  'Maharashtra',
  '411045'
);

INSERT INTO quotation_counters (company_id, next_value)
VALUES ('11111111-1111-4111-8111-111111111111', 1);

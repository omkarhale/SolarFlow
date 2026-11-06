# Database

PostgreSQL 16. Schema changes only through Flyway in `backend/src/main/resources/db/migration`. Hibernate validates the mapping and does not create tables.

`V1__create_schema.sql` creates the business tables. `V2__seed_demo_company.sql` inserts one fictional company, Helios Rooftop Solar in Pune, and its quotation counter. No user is seeded. A password will be introduced with authentication tests in Phase 2.

## Entity relationship

```mermaid
erDiagram
  companies ||--o{ users : employs
  companies ||--o{ customers : owns
  companies ||--o{ leads : owns
  companies ||--o{ products : stocks
  companies ||--|| quotation_counters : numbers
  users ||--o{ refresh_tokens : sessions
  customers ||--o{ leads : may_match
  leads ||--o{ site_surveys : surveyed
  site_surveys ||--o{ survey_photos : photos
  leads ||--o{ quotations : quoted
  quotations ||--o{ quotation_lines : lines
  products ||--o{ quotation_lines : priced
  products ||--o{ stock_movements : moves
  customers ||--o{ projects : installs
  quotations ||--o{ projects : accepted
  projects ||--o{ project_tasks : tasks
  projects ||--o{ project_photos : photos
  customers ||--o{ payments : pays
  projects ||--o{ payments : billed
  projects ||--o{ warranties : covers
  warranties ||--o{ service_requests : claimed
  users ||--o{ notifications : receives
```

## Tenant rule

Company-owned tables include `company_id`. Where a child references a parent, the foreign key includes `company_id`. Nullable assignees use the same pair, so PostgreSQL checks the user only when an assignee is set, and that user must belong to the same company.

## Money and quantities

Currency and totals are `NUMERIC(14,2)`. Capacities are `NUMERIC(8,2)`. Stock quantities are `NUMERIC(12,2)`. Application code uses `BigDecimal`.

## Indexes

List and pipeline screens filter by company plus status, follow-up date, payment date, warranty end, or notification time. Those pairs are indexed in `V1`.

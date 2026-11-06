# SolarFlow project log

This file is the running record of what has been built. It is updated after each working session.

## Current status

Phase 2 through Phase 9 are in the product: sign-in, customers, leads, surveys, quotations, projects, payments, inventory, service, and reports. Appearance includes eight light themes, four dark themes, and a custom theme. Docker is not part of the project.

## 6 November 2026 — Themes

Light themes: Solar Navy, Harbor, Dune, Slate, Indigo Desk, Evergreen, Aurora, Lagoon. Dark themes: Midnight, Obsidian, Dusk, Ember. A custom theme derives status chips from the background so a dark canvas stays readable. `npm test` passed, 4 tests.

## 6 November 2026 — Remaining phases

### What landed

- JWT access tokens (15 minutes) and rotating refresh tokens in the `solarflow_refresh` HttpOnly cookie. Reuse of a revoked token revokes the family. Logout, logout-all, and change-password revoke sessions. CSRF uses the `XSRF-TOKEN` cookie.
- Company comes from the signed-in user. A customer that belongs to another company returns 404.
- APIs for customers, leads, surveys and photos, quotations with preview and PDF, projects and tasks, payments and receipts, inventory movements, warranties, service requests, dashboard, reports, company profile, team, and notifications.
- Demo data for Helios Rooftop Solar when `solarflow.demo-data` is true and `owner@helios.example` is absent. Password `Solarflow#2026`. Tests turn demo data off.
- The React workspace signs in, keeps the access token in memory, and loads each module from the API. Empty lists stay empty when the company has no rows.

### Verification

| Check | Result |
| --- | --- |
| `mvn test` | 12 passed, 0 failed. Exit code 0 |
| `npm run lint` | Passed, no warnings |
| `npm test` | 4 passed, 0 failed |
| `npm run build` | Passed |
| Sign-in page | `http://localhost:5173/login` renders the email and password form |

Backend tests:

- `AuthFlowTests`: 4 passed
- `QuotationPricingTest`: 2 passed
- `TenantGuardsTest`: 3 passed
- `SolarFlowApplicationTests`: 3 passed

The installed PostgreSQL on port 5432 rejected user `solarflow` with the password in `.env.example`. Create that role and database with the SQL in the README before `mvn spring-boot:run` against it. A browser walk-through of a saved customer was not completed in this session because the sign-in form could not be submitted again after the first attempt returned to the login page.

## 6 November 2026 — Project photos

Phase 6 was missing installation photos. A project can now take a PNG or JPG, store it under `storage/`, and return it only to the same company. The project page shows those photos and can move a task between to do, in progress, and done. Survey photos already stored by the API now show on the survey page.

The refresh cookie path is `/api`, so the browser sends it on refresh after sign-in.

### Verification

| Check | Result |
| --- | --- |
| `AuthFlowTests` | 5 passed, including upload and download of a project photo |
| `QuotationPricingTest` | 2 passed |
| `npm run lint` | Passed, no warnings |
| `npm test` | 4 passed |
| `npm run build` | Passed |

### Remaining

- Several business rules still live in controllers. Auth is in `AuthService`.
- Survey and project photo galleries are upload-only in the UI. The files are stored under `storage/`.
- The local PostgreSQL role still has to match `DB_USERNAME` and `DB_PASSWORD`.

## 6 November 2026 — Foundation

The workspace was empty. Phase 0 and Phase 1 were created together.

### Product shape

- Modular monolith for a multi-tenant solar installation company.
- Tenant key is `company_id`. Child rows also store `company_id`, and foreign keys use the pair so a record cannot point at another company's parent.
- `TenantGuards.sameCompany` treats a mismatch as not found.
- Money columns are `NUMERIC` and will be handled as `BigDecimal`.
- API prefix is `/api/v1`. JPA entities are not exposed. Hibernate validates the schema and does not create tables.

### Backend

- Java 21, Spring Boot 3.4.5, Spring Security, Spring Data JPA, Flyway, OpenAPI.
- Packages for auth, company, user, customer, lead, survey, quotation, project, inventory, payment, warranty, service, notification, and health.
- Flyway `V1__create_schema.sql` creates the business tables, checks, indexes, and `updated_at` triggers.
- Flyway `V2__seed_demo_company.sql` inserts the fictional company Helios Rooftop Solar in Pune and a quotation counter. No user is seeded.
- `GET /api/v1/health` checks the database. Other routes require authentication. There is no login yet, so they return 401.

### Frontend

- React, TypeScript, Vite, Tailwind, React Router, TanStack Query, Axios.
- Application shell with a collapsible sidebar, mobile drawer, search across destinations, notifications empty state, breadcrumbs, and account menu.
- Design-system components for layout, forms, tables, charts, and feedback.
- Module pages are honest empty states. They do not show fake customers, revenue, or leads.
- Design tokens live in `frontend/src/styles/globals.css` and `frontend/tailwind.config.ts`.

### Docs

- `README.md`, `docs/ARCHITECTURE.md`, `docs/DATABASE.md`, `docs/DESIGN_SYSTEM.md`, `docs/DEVELOPMENT_PHASES.md`, `docs/TESTING.md`, `docs/DEPLOYMENT.md`, `docs/BACKUP.md`.

## 6 November 2026 — Remove Docker and add themes

### Docker

Removed, as requested:

- `docker-compose.yml`
- `backend/Dockerfile` and `backend/.dockerignore`
- `frontend/Dockerfile` and `frontend/nginx.conf`

Local setup now expects a PostgreSQL instance you run yourself. Backend tests use an embedded PostgreSQL library instead of Docker. The first test run downloads those binaries. They are not committed.

### Themes

The workspace has six standard themes and a custom theme. The choice is stored in the browser under `solarflow.theme`.

Standard themes:

- Solar Navy, the default
- Harbor
- Dune
- Slate
- Indigo Desk
- Evergreen

Custom theme controls: primary, accent, background, surface, and text. Status colors stay fixed so success, warning, danger, and information keep their meaning. If the primary color is light, button text switches to dark.

Where to change it:

- Settings, under Appearance
- The palette button in the top bar

A small script in `index.html` applies the saved theme before the app paints.

### Verification in this session

Executed, not assumed:

| Check | Result |
| --- | --- |
| `npm run lint` | Passed, no warnings |
| `npm test` | 4 passed, 0 failed |
| `npm run build` | Passed |
| `mvn test` | 6 passed, 0 failed. `BUILD SUCCESS` |
| Theme UI | Harbor, Dune, a custom primary, reset to Solar Navy, and navigation back to the dashboard |

Backend tests:

- `TenantGuardsTest`: 3 passed
- `SolarFlowApplicationTests`: 3 passed (health, 401 on a protected route, Helios seed)

The dashboard showed an error state in that session because the API was not running.

Those three remaining items were done in the following section.

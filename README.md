# SolarFlow

SolarFlow is a multi-tenant workspace for residential rooftop solar companies in India. It follows an enquiry from the first call through survey, quotation, installation, payment, and service.

Sign in at `http://localhost:5173/login`. The seeded company is Helios Rooftop Solar. The owner is `owner@helios.example` / `Solarflow#2026`. Sales and technician accounts use the same password: `sales@helios.example` and `tech@helios.example`. Demo records load only when `solarflow.demo-data` is true and that owner does not already exist.

## Stack

- Frontend: React, TypeScript, Vite, Tailwind CSS
- Backend: Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA, Flyway
- Database: PostgreSQL 16

## Local setup

Requirements: JDK 21, Node.js 22+, and a local PostgreSQL 16 database named `solarflow`.

1. Create the database and user if they do not exist:

```sql
CREATE USER solarflow WITH PASSWORD '1234567';
CREATE DATABASE solarflow OWNER solarflow;
```

2. Backend, from `backend`:

```bash
mvn test
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. OpenAPI is at `http://localhost:8080/swagger-ui.html`.

3. Frontend, from `frontend`:

```bash
npm install
npm run dev
```

The app listens on `http://localhost:5173` and proxies `/api` to the backend.

Copy `.env.example` if the database host, port, or password differ from the defaults. Theme choice is stored in the browser.

## Scripts

| Location | Command | Purpose |
| --- | --- | --- |
| backend | `mvn test` | Unit tests and PostgreSQL integration tests |
| backend | `mvn -DskipTests package` | Production jar |
| frontend | `npm run lint` | ESLint |
| frontend | `npm test` | Vitest |
| frontend | `npm run build` | Typecheck and production build |

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Database](docs/DATABASE.md)
- [Design system](docs/DESIGN_SYSTEM.md)
- [Phases](docs/DEVELOPMENT_PHASES.md)
- [Testing](docs/TESTING.md)
- [Deployment](docs/DEPLOYMENT.md)
- [Backup and recovery](docs/BACKUP.md)
- [Phase status and session log](PROJECT_LOG.md)

## Phase status

Phase 2 through Phase 9 are in the workspace: authentication, customers, leads, surveys, quotations, projects, payments, inventory, warranty, service, and reports. Records come from the API. Theme choice stays in the browser.

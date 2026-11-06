# Testing

## Backend

`mvn test` runs:

- `TenantGuardsTest` — same-company checks, including a missing company id
- `AuthFlowTests` — login, refresh rotation, reuse of a revoked refresh token, logout, logout-all, password change, and a 404 when another company reads a customer
- `QuotationPricingTest` — discount before GST, and a discount above the subtotal
- `SolarFlowApplicationTests` — embedded PostgreSQL, Flyway, health, 401 on `/api/v1/auth/me`, Helios seed. Demo business data is off.

The integration test does not use Docker. The first run downloads the embedded PostgreSQL binaries.

## Frontend

`npm test` covers the primary button, the sidebar destinations, the time-of-day greeting, and custom theme contrast.

`npm run lint` and `npm run build` are part of the phase gate.

## Manual API check

With PostgreSQL and the API running:

```bash
curl http://localhost:8080/api/v1/health
```

A healthy response has `"status":"ok"` and `"database":"up"`.

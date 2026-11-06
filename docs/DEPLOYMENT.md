# Deployment

Run PostgreSQL 16 on the host or on a managed database. SolarFlow does not ship Docker configuration.

Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the API. Set `CORS_ALLOWED_ORIGINS` to the public site origin.

Build the API jar from `backend`:

```bash
mvn -DskipTests package
```

The jar is `backend/target/solarflow-backend-0.1.0.jar`. Run it with `java -jar`.

Build the frontend from `frontend` with `npm run build`. Serve the `dist` folder from any static host and proxy `/api` to the API process.

No paid external service is required. File uploads, when added, will use local disk until object storage is explicitly chosen.

# Backup and recovery

PostgreSQL is the system of record.

## Backup

```bash
pg_dump -h localhost -U solarflow -d solarflow -Fc -f solarflow.dump
```

Take a dump before applying a Flyway migration in any shared environment. Store dumps outside the application host.

## Restore

```bash
pg_restore -h localhost -U solarflow -d solarflow --clean --if-exists solarflow.dump
```

Restore into an empty database when possible. `--clean` drops objects that exist in the dump and will remove data.

## After restore

Start the API and confirm `GET /api/v1/health` reports the database as up. Flyway will not reapply migrations that are already recorded in `flyway_schema_history`.

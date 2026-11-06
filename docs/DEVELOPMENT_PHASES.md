# Development phases

Work is sequential. A phase is finished only after its tests, builds, and the notes in `PHASE_STATUS.md` are updated from commands that were actually run.

| Phase | Scope |
| --- | --- |
| 0 | Architecture, schema design, design tokens, phase plan |
| 1 | Repository foundation, Flyway schema, health API, design system, application shell |
| 2 | JWT access tokens, refresh rotation, logout, CSRF, users |
| 3 | Dashboard metrics and customers |
| 4 | Lead pipeline |
| 5 | Site surveys and quotations, including PDF |
| 6 | Projects, tasks, photos |
| 7 | Payments and inventory |
| 8 | Warranty and service |
| 9 | Reports and a full design review |

Phases 2–9 were completed in one pass after Phase 1. The gate below was run once at the end of that pass, and the results are in `PROJECT_LOG.md`.

## Gate after each phase

1. Review the diff for layering, tenant filters, and duplicated UI.
2. `mvn test` in `backend`.
3. `npm run lint`, `npm test`, and `npm run build` in `frontend`.
4. Exercise the new API and the screens that depend on it.
5. Record features, files, APIs, migrations, test results, and open issues in `PHASE_STATUS.md`.

The next phase starts only when that gate is green.

# CMS Quickstart

This repository contains a minimal internal CMS MVP with a React portal/admin UI, Spring Boot REST API, PostgreSQL migrations, and Docker preview stack.

## Preview

Run the preview stack with Docker Compose:

```bash
docker compose up --build
```

Open `http://localhost:5173`. The frontend proxies API requests to the `api` service. The API is also published at `http://localhost:8080`.

Development personas are selected from the header:

- `Employee`: portal search, content detail, attachments, acknowledgements
- `HR Employee` / `Engineering Employee`: targeted-audience visibility validation
- `Author`: draft creation and submission
- `HR Author`: HR-targeted content validation
- `Reviewer`: review queue, approve/reject, publish
- `HR Reviewer` / `HR Publisher`: quickstart review and publication handoff
- `Notice Manager`: targeted notice and acknowledgement reporting
- `Auditor`: audit and content-health reporting
- `Editor`: acknowledgement dashboard
- `Admin`: taxonomy, access governance, audit log

## Database

PostgreSQL migrations live in `infra/db/migrations`.

The Docker image sets `SPRING_FLYWAY_ENABLED=true` and applies those migrations on startup. PostgreSQL only runs init scripts for a fresh volume; if an existing `cms_db_data` volume is present, Flyway is the migration mechanism and the database entrypoint will not replay initial scripts.

## Operations

- Health: `GET http://localhost:8080/actuator/health`
- Logs: `docker compose logs -f api frontend db`
- Rebuild images: `docker compose build --no-cache api frontend`
- Restart services: `docker compose restart api frontend`
- Backup: `docker compose exec db pg_dump -U cms cms > cms-backup.sql`
- Restore: `cat cms-backup.sql | docker compose exec -T db psql -U cms cms`

Attachment metadata is stored by the API. The MVP uses generated storage references; production should replace this with approved object or document storage while retaining the authorization checks in the API.

## Contract Paths

The API supports both the original MVP paths and the generated contract aliases for notices, bookmarks, audit search, and content health:

- `GET /api/v1/notices/pending`
- `POST /api/v1/notices/{noticeId}/acknowledgements`
- `GET /api/v1/notices/{noticeId}/acknowledgements/report`
- `GET|POST /api/v1/bookmarks`
- `GET /api/v1/audit-events`
- `GET /api/v1/reports/content-health`

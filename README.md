# CMS4

Internal CMS MVP for company knowledge articles, document records, notices, acknowledgements, governance, and audit.

## Structure

- `backend/`: Spring Boot REST API with controller, service, repository, domain, DTO, and security layers
- `frontend/`: React TypeScript portal, authoring, review, editor, and admin UI
- `infra/db/migrations/`: PostgreSQL schema and search migrations
- `docs/cms-quickstart.md`: preview, validation, backup, restore, and operations notes

## Branch Preview

```bash
docker compose up --build
```

Open `http://localhost:5173`. The API is published at `http://localhost:8080`.

The UI header includes a development persona selector for Employee, Author, Reviewer, Editor, and Admin workflows.

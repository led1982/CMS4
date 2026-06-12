# CMS Operations Runbook

## Health

- API health: `GET /actuator/health`
- Frontend preview: `http://localhost:5173`
- API preview: `http://localhost:8080`

## Docker Operations

```bash
docker compose up --build
docker compose logs -f api frontend db
docker compose restart api frontend
docker compose build --no-cache api frontend
```

The alternate compose file at `infra/docker-compose.yml` includes a local file-storage volume for attachment preservation drills.

## Backup And Restore

```bash
docker compose exec db pg_dump -U cms cms > cms-backup.sql
cat cms-backup.sql | docker compose exec -T db psql -U cms cms
```

Coordinate database backups with the upload/file-storage volume. Metadata without binaries, or binaries without metadata, is not a complete restore.

## Incident Checks

1. Check `/actuator/health` for application and database status.
2. Review `docker compose logs -f api` for request IDs and sanitized errors.
3. Confirm restricted content failures are `403` and do not expose titles or attachment names.
4. Confirm attachment size failures return `413`.
5. Validate Flyway status before deploying schema changes to an existing volume.

# CMS Configuration

## Required Runtime Variables

- `SPRING_DATASOURCE_URL`: PostgreSQL JDBC URL.
- `SPRING_DATASOURCE_USERNAME`: PostgreSQL user.
- `SPRING_DATASOURCE_PASSWORD`: PostgreSQL password.
- `SPRING_FLYWAY_ENABLED`: set to `true` for managed database migration.
- `SPRING_FLYWAY_LOCATIONS`: migration path. Docker uses `filesystem:/app/infra/db/migrations`.
- `CMS_CORS_ALLOWED_ORIGINS`: comma-separated frontend origins.
- `CMS_LOG_LEVEL`: backend package log level, default `INFO`.
- `CMS_LIFECYCLE_SCAN_DELAY_MS`: optional scheduled publication/expiration scan interval.
- `CMS_REPORTING_HEALTH_SCAN_DELAY_MS`: optional stale-content scan interval.

## Upload Limits

The backend sets `spring.servlet.multipart.max-file-size=10MB` and `spring.servlet.multipart.max-request-size=20MB`.
The MVP metadata upload endpoint also rejects a single attachment above 10 MB with HTTP `413`.

## Database Initialization

Flyway migrations are repeatable for new environments. PostgreSQL container init scripts only run for an empty volume; when `cms_db_data` already exists, use Flyway migrations or recreate the volume intentionally.

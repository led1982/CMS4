CREATE TABLE IF NOT EXISTS backup_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backup_type TEXT NOT NULL,
    status TEXT NOT NULL,
    storage_reference TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    details JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS file_storage_objects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attachment_id UUID REFERENCES attachments(id) ON DELETE CASCADE,
    storage_provider TEXT NOT NULL DEFAULT 'local-adapter',
    storage_reference TEXT NOT NULL,
    checksum TEXT,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    retained_until TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_time ON audit_logs(actor_user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_content_items_expiring ON content_items(expires_at) WHERE status = 'PUBLISHED';
CREATE INDEX IF NOT EXISTS idx_file_storage_objects_attachment ON file_storage_objects(attachment_id);

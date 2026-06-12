CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    saved_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, content_item_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user_saved ON bookmarks(user_id, saved_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor_time ON audit_logs(actor_user_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_content_items_expiring ON content_items(expires_at) WHERE status = 'PUBLISHED';

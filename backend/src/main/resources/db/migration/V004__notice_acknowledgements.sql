CREATE TABLE IF NOT EXISTS acknowledgements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_id UUID NOT NULL REFERENCES content_versions(id),
    user_id UUID NOT NULL REFERENCES users(id),
    acknowledged_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    source TEXT NOT NULL DEFAULT 'PORTAL',
    UNIQUE(content_item_id, version_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_acknowledgements_content ON acknowledgements(content_item_id, acknowledged_at DESC);
CREATE INDEX IF NOT EXISTS idx_acknowledgements_user ON acknowledgements(user_id, acknowledged_at DESC);

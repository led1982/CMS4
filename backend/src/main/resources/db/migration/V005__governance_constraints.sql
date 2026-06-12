CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    normalized_name TEXT NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS content_tags (
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id),
    PRIMARY KEY (content_item_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_content_items_audience_status ON content_items(audience_id, status);
CREATE INDEX IF NOT EXISTS idx_content_tags_tag ON content_tags(tag_id);

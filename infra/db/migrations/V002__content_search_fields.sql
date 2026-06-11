ALTER TABLE content_items
    ADD COLUMN search_document TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(summary, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(body, '')), 'C')
    ) STORED;

CREATE INDEX idx_content_items_search_document ON content_items USING GIN (search_document);
CREATE INDEX idx_content_items_priority_published ON content_items(priority, published_at DESC)
    WHERE status = 'PUBLISHED';
CREATE INDEX idx_content_items_effective_window ON content_items(effective_from, expires_at)
    WHERE status IN ('PUBLISHED', 'SCHEDULED');

CREATE TABLE search_indexes (
    content_item_id UUID PRIMARY KEY REFERENCES content_items(id) ON DELETE CASCADE,
    title_text TEXT NOT NULL,
    body_text TEXT,
    tag_text TEXT,
    language_hint TEXT NOT NULL DEFAULT 'ko-en',
    indexed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

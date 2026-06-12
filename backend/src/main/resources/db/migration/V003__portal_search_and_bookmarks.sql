CREATE TABLE IF NOT EXISTS search_indexes (
    content_item_id UUID PRIMARY KEY REFERENCES content_items(id) ON DELETE CASCADE,
    title_text TEXT NOT NULL,
    body_text TEXT,
    tag_text TEXT,
    language_hint TEXT NOT NULL DEFAULT 'ko-en',
    indexed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    saved_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, content_item_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user_saved ON bookmarks(user_id, saved_at DESC);
CREATE INDEX IF NOT EXISTS idx_search_indexes_title ON search_indexes USING GIN (to_tsvector('simple', title_text));

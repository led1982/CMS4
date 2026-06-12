CREATE TABLE IF NOT EXISTS audiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    audience_type TEXT NOT NULL,
    criteria JSONB NOT NULL DEFAULT '{}'::jsonb,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES categories(id),
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    description TEXT,
    owner_user_id UUID REFERENCES users(id),
    default_audience_id UUID REFERENCES audiences(id),
    default_reviewer_group_id TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(parent_id, name),
    UNIQUE(parent_id, slug)
);

CREATE TABLE IF NOT EXISTS content_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type TEXT NOT NULL,
    title TEXT NOT NULL,
    slug TEXT NOT NULL,
    summary TEXT,
    body TEXT,
    status TEXT NOT NULL,
    owner_user_id UUID REFERENCES users(id),
    author_user_id UUID REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    audience_id UUID REFERENCES audiences(id),
    priority TEXT NOT NULL DEFAULT 'NORMAL',
    requires_acknowledgement BOOLEAN NOT NULL DEFAULT FALSE,
    effective_from TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ,
    archived_at TIMESTAMPTZ,
    current_published_version_id UUID,
    draft_version_id UUID,
    revision_number INTEGER NOT NULL DEFAULT 1,
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS content_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    status TEXT NOT NULL,
    title_snapshot TEXT NOT NULL,
    summary_snapshot TEXT,
    body_snapshot TEXT,
    change_summary TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(content_item_id, version_number)
);

CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_id UUID REFERENCES content_versions(id),
    file_name TEXT NOT NULL,
    mime_type TEXT NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    checksum TEXT,
    storage_reference TEXT NOT NULL,
    upload_status TEXT NOT NULL,
    validation_status TEXT NOT NULL,
    validation_message TEXT,
    uploaded_by UUID REFERENCES users(id),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS review_decisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_id UUID REFERENCES content_versions(id),
    reviewer_user_id UUID REFERENCES users(id),
    decision TEXT NOT NULL,
    comments TEXT,
    decided_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_categories_parent_sort ON categories(parent_id, sort_order, name);
CREATE INDEX IF NOT EXISTS idx_content_items_status_updated ON content_items(status, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_content_items_category_status ON content_items(category_id, status);
CREATE INDEX IF NOT EXISTS idx_attachments_content ON attachments(content_item_id);

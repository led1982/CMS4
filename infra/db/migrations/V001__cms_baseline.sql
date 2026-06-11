CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id TEXT UNIQUE,
    display_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    department TEXT NOT NULL,
    job_title TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    identity_provider_subject TEXT,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    description TEXT,
    system_managed BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE audiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    audience_type TEXT NOT NULL,
    criteria JSONB NOT NULL DEFAULT '{}'::jsonb,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reviewer_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES categories(id),
    name TEXT NOT NULL,
    slug TEXT NOT NULL,
    description TEXT,
    owner_user_id UUID REFERENCES users(id),
    default_audience_id UUID REFERENCES audiences(id),
    default_reviewer_group_id UUID REFERENCES reviewer_groups(id),
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(parent_id, name),
    UNIQUE(parent_id, slug)
);

CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    normalized_name TEXT NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE role_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    scope_type TEXT NOT NULL,
    scope_id TEXT,
    assigned_by UUID REFERENCES users(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ
);

CREATE TABLE content_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_type TEXT NOT NULL,
    title TEXT NOT NULL,
    slug TEXT NOT NULL,
    summary TEXT,
    body TEXT,
    status TEXT NOT NULL,
    owner_user_id UUID NOT NULL REFERENCES users(id),
    author_user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    audience_id UUID NOT NULL REFERENCES audiences(id),
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

CREATE TABLE content_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    title_snapshot TEXT NOT NULL,
    summary_snapshot TEXT,
    body_snapshot TEXT,
    metadata_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    change_summary TEXT,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(content_item_id, version_number)
);

ALTER TABLE content_items
    ADD CONSTRAINT fk_current_published_version
        FOREIGN KEY (current_published_version_id) REFERENCES content_versions(id),
    ADD CONSTRAINT fk_draft_version
        FOREIGN KEY (draft_version_id) REFERENCES content_versions(id);

CREATE TABLE content_tags (
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id),
    PRIMARY KEY (content_item_id, tag_id)
);

CREATE TABLE attachments (
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
    uploaded_by UUID NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE file_storage_objects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attachment_id UUID REFERENCES attachments(id) ON DELETE CASCADE,
    storage_provider TEXT NOT NULL DEFAULT 'local-adapter',
    storage_reference TEXT NOT NULL,
    checksum TEXT,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    retained_until TIMESTAMPTZ
);

CREATE TABLE review_decisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_id UUID REFERENCES content_versions(id),
    reviewer_user_id UUID NOT NULL REFERENCES users(id),
    decision TEXT NOT NULL,
    comments TEXT,
    decided_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE acknowledgements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_item_id UUID NOT NULL REFERENCES content_items(id) ON DELETE CASCADE,
    version_id UUID NOT NULL REFERENCES content_versions(id),
    user_id UUID NOT NULL REFERENCES users(id),
    acknowledged_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    source TEXT NOT NULL DEFAULT 'PORTAL',
    UNIQUE(content_item_id, version_id, user_id)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id UUID NOT NULL REFERENCES users(id),
    event_type TEXT NOT NULL,
    content_item_id UUID REFERENCES content_items(id),
    message TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID REFERENCES users(id),
    action TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_id TEXT NOT NULL,
    summary TEXT NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    request_id TEXT
);

CREATE TABLE system_settings (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID REFERENCES users(id)
);

CREATE TABLE backup_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backup_type TEXT NOT NULL,
    status TEXT NOT NULL,
    storage_reference TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at TIMESTAMPTZ,
    details JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_categories_parent_sort ON categories(parent_id, sort_order, name);
CREATE INDEX idx_content_items_status_updated ON content_items(status, updated_at DESC);
CREATE INDEX idx_content_items_category_status ON content_items(category_id, status);
CREATE INDEX idx_content_items_audience_status ON content_items(audience_id, status);
CREATE INDEX idx_attachments_content ON attachments(content_item_id);
CREATE INDEX idx_file_storage_objects_attachment ON file_storage_objects(attachment_id);
CREATE INDEX idx_audit_logs_target ON audit_logs(target_type, target_id, occurred_at DESC);
CREATE INDEX idx_acknowledgements_content ON acknowledgements(content_item_id, acknowledged_at DESC);

INSERT INTO roles (code, name, description) VALUES
    ('EMPLOYEE', 'Employee', 'Can browse and read eligible published content'),
    ('AUTHOR', 'Author', 'Can create drafts and submit content for review'),
    ('REVIEWER', 'Reviewer', 'Can approve, reject, or request changes'),
    ('EDITOR', 'Editor', 'Can manage lifecycle dashboards and reports'),
    ('ADMINISTRATOR', 'Administrator', 'Can manage governance and audit');

INSERT INTO users (id, employee_id, display_name, email, department, job_title, identity_provider_subject) VALUES
    ('00000000-0000-0000-0000-000000000001', 'E001', 'Employee User', 'employee@example.com', 'Operations', 'Employee', 'dev-employee'),
    ('00000000-0000-0000-0000-000000000002', 'A001', 'Author User', 'author@example.com', 'People', 'Content Author', 'dev-author'),
    ('00000000-0000-0000-0000-000000000003', 'R001', 'Reviewer User', 'reviewer@example.com', 'People', 'Reviewer', 'dev-reviewer'),
    ('00000000-0000-0000-0000-000000000004', 'ED01', 'Editor User', 'editor@example.com', 'Communications', 'Editor', 'dev-editor'),
    ('00000000-0000-0000-0000-000000000005', 'AD01', 'Admin User', 'admin@example.com', 'IT', 'Administrator', 'dev-admin');

INSERT INTO audiences (id, name, audience_type, criteria, created_by) VALUES
    ('10000000-0000-0000-0000-000000000001', 'All Employees', 'ALL_COMPANY', '{"scope":"all"}', '00000000-0000-0000-0000-000000000005');

INSERT INTO reviewer_groups (id, name, description) VALUES
    ('20000000-0000-0000-0000-000000000001', 'HR Policy Reviewers', 'Default reviewers for HR policies');

INSERT INTO categories (id, name, slug, description, owner_user_id, default_audience_id, default_reviewer_group_id, sort_order) VALUES
    ('30000000-0000-0000-0000-000000000001', 'HR Policies', 'hr-policies', 'Policies, benefits, and compliance notices', '00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 10),
    ('30000000-0000-0000-0000-000000000002', 'IT Guides', 'it-guides', 'Internal technology guides and procedures', '00000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 20);

INSERT INTO tags (id, name, normalized_name, description) VALUES
    ('40000000-0000-0000-0000-000000000001', 'Policy', 'policy', 'Official policy content'),
    ('40000000-0000-0000-0000-000000000002', 'Security', 'security', 'Security awareness and IT guidance');

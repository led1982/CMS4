CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    code TEXT NOT NULL UNIQUE,
    parent_department_id UUID REFERENCES departments(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id TEXT UNIQUE,
    display_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    department_id UUID REFERENCES departments(id),
    department TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    identity_provider_subject TEXT,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    permissions JSONB NOT NULL DEFAULT '[]'::jsonb,
    system_managed BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS role_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    scope_type TEXT NOT NULL DEFAULT 'GLOBAL',
    scope_id TEXT,
    assigned_by UUID REFERENCES users(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS audit_logs (
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

CREATE TABLE IF NOT EXISTS system_settings (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by UUID REFERENCES users(id)
);

INSERT INTO roles (code, name, permissions) VALUES
    ('EMPLOYEE', 'Employee', '["PORTAL_READ"]'),
    ('AUTHOR', 'Author', '["CONTENT_CREATE","CONTENT_EDIT"]'),
    ('REVIEWER', 'Reviewer', '["CONTENT_REVIEW"]'),
    ('PUBLISHER', 'Publisher', '["CONTENT_PUBLISH"]'),
    ('NOTICE_MANAGER', 'Notice Manager', '["NOTICE_MANAGE","ACK_REPORT"]'),
    ('EDITOR', 'Editor', '["CONTENT_EDIT","CONTENT_REVIEW","CONTENT_PUBLISH","ACK_REPORT"]'),
    ('AUDITOR', 'Auditor', '["AUDIT_READ"]'),
    ('ADMINISTRATOR', 'Administrator', '["ADMIN_TAXONOMY","ADMIN_ACCESS","AUDIT_READ"]')
ON CONFLICT (code) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_audit_logs_target ON audit_logs(target_type, target_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_role_assignments_user ON role_assignments(user_id, assigned_at DESC);

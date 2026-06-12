# CMS Scope UI

The Scope navigation item opens the request dashboard at `/scope-requests`.

## Screens

- Dashboard: lists request code, status, domain count, artifact set count, and latest validation result.
- Import Request: captures CMS4-4 fields, domain lists, dependency lines, acceptance criteria, and source text.
- Request Detail: shows assumptions, generation order, domain matrix, artifact coverage, and validation findings.
- Artifact Preview: switches between `spec.md`, `plan.md`, and `tasks.md` for a generated domain.

## Key States

- Empty dashboard: displays an import action.
- Imported but unresolved: detail page has no domains and offers Resolve Domains.
- Resolved default domain: `cms-core` appears in stage 1 and assumptions explain the default.
- Generated: artifact table shows `spec.md`, `plan.md`, and `tasks.md` rows.
- Validated: validation panel shows `passed` or actionable findings with severity, type, domain, and recommended action.

All status indicators include text labels and keyboard-focusable controls use native links, buttons, tabs, and form fields.

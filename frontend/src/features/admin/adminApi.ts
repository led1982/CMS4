import { apiRequest, Audience, AuditEvent, Category, RoleAssignment, Tag } from "../../services/apiClient";

export function createCategory(request: { parentId?: string; name: string; description?: string; ownerUserId: string; defaultAudienceId?: string; sortOrder?: number }) {
  return apiRequest<Category>("/api/v1/categories", { method: "POST", body: JSON.stringify(request) });
}

export function createTag(request: { name: string; description?: string }) {
  return apiRequest<Tag>("/api/v1/tags", { method: "POST", body: JSON.stringify(request) });
}

export function listTags() {
  return apiRequest<Tag[]>("/api/v1/tags");
}

export function listAudiences() {
  return apiRequest<Audience[]>("/api/v1/admin/audiences");
}

export function createAudience(request: { name: string; audienceType: string; criteria: Record<string, unknown> }) {
  return apiRequest<Audience>("/api/v1/admin/audiences", { method: "POST", body: JSON.stringify(request) });
}

export function listRoleAssignments(userId?: string) {
  return apiRequest<RoleAssignment[]>(`/api/v1/admin/role-assignments${userId ? `?userId=${encodeURIComponent(userId)}` : ""}`);
}

export function createRoleAssignment(request: { userId: string; role: string; scopeType: string; scopeId?: string }) {
  return apiRequest<RoleAssignment>("/api/v1/admin/role-assignments", { method: "POST", body: JSON.stringify(request) });
}

export function searchAuditEvents(params: { action?: string; targetType?: string } = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => value && searchParams.set(key, value));
  return apiRequest<{ items: AuditEvent[]; page: { totalItems: number } }>(`/api/v1/admin/audit-events?${searchParams.toString()}`);
}

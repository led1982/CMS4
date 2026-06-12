import { apiRequest } from "../../services/apiClient";
import type { AuditEvent, ContentSummary } from "../../services/apiClient";

export interface ContentHealthReport {
  stale: ContentSummary[];
  expiring: ContentSummary[];
  archived: ContentSummary[];
  recentlyPublished: ContentSummary[];
}

export function getContentHealth() {
  return apiRequest<ContentHealthReport>("/api/v1/reports/content-health");
}

export function searchAuditEvents(params: { action?: string; targetType?: string; targetId?: string } = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => value && searchParams.set(key, value));
  return apiRequest<{ items: AuditEvent[]; page: { totalItems: number } }>(`/api/v1/audit-events?${searchParams.toString()}`);
}

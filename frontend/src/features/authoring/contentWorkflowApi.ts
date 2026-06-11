import { apiRequest, AttachmentMetadata, ContentCreateRequest, ContentDetail, ContentSummary, ContentUpdateRequest } from "../../services/apiClient";

export function createContent(request: ContentCreateRequest) {
  return apiRequest<ContentDetail>("/api/v1/content", { method: "POST", body: JSON.stringify(request) });
}

export function updateContent(contentId: string, request: ContentUpdateRequest) {
  return apiRequest<ContentDetail>(`/api/v1/content/${contentId}`, { method: "PATCH", body: JSON.stringify(request) });
}

export function submitForReview(contentId: string, versionToken: string, changeSummary: string) {
  return apiRequest<ContentDetail>(`/api/v1/content/${contentId}/submit-review`, {
    method: "POST",
    body: JSON.stringify({ versionToken, changeSummary })
  });
}

export function publishContent(contentId: string, versionToken: string) {
  return apiRequest<ContentDetail>(`/api/v1/content/${contentId}/publish`, {
    method: "POST",
    body: JSON.stringify({ versionToken })
  });
}

export function attachFile(contentId: string, request: { fileName: string; mimeType: string; sizeBytes: number; checksum?: string }) {
  return apiRequest<AttachmentMetadata>(`/api/v1/content/${contentId}/attachments`, { method: "POST", body: JSON.stringify(request) });
}

export function getReviewQueue() {
  return apiRequest<ContentSummary[]>("/api/v1/review/queue");
}

export function reviewContent(contentId: string, versionToken: string, decision: "APPROVE" | "REJECT" | "REQUEST_CHANGES", comments?: string) {
  return apiRequest<ContentDetail>(`/api/v1/content/${contentId}/review`, {
    method: "POST",
    body: JSON.stringify({ versionToken, decision, comments })
  });
}

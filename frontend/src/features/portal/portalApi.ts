import {
  apiRequest,
  AttachmentMetadata,
  AcknowledgementReport,
  Category,
  ContentDetail,
  ContentListResponse,
  ContentStatus,
  ContentType,
  PortalHome
} from "../../services/apiClient";

export function getPortalHome() {
  return apiRequest<PortalHome>("/api/v1/portal/home");
}

export function searchContent(params: {
  q?: string;
  contentType?: ContentType;
  categoryId?: string;
  tag?: string;
  status?: ContentStatus;
  acknowledgementRequired?: boolean;
  page?: number;
  size?: number;
}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") {
      searchParams.set(key, String(value));
    }
  });
  return apiRequest<ContentListResponse>(`/api/v1/content?${searchParams.toString()}`);
}

export function getContent(contentId: string) {
  return apiRequest<ContentDetail>(`/api/v1/content/${contentId}`);
}

export function getCategories() {
  return apiRequest<Category[]>("/api/v1/categories");
}

export function listAttachments(contentId: string) {
  return apiRequest<AttachmentMetadata[]>(`/api/v1/content/${contentId}/attachments`);
}

export function getAttachmentDownload(contentId: string, attachmentId: string) {
  return apiRequest<{ attachmentId: string; downloadUrl: string; expiresAt: string }>(
    `/api/v1/content/${contentId}/attachments/${attachmentId}/download`
  );
}

export function getAcknowledgementReport(contentId: string) {
  return apiRequest<AcknowledgementReport>(`/api/v1/editor/acknowledgements?contentId=${encodeURIComponent(contentId)}`);
}

export function createBookmark(contentId: string) {
  return apiRequest<{ id: string; savedAt: string }>(`/api/v1/bookmarks`, { method: "POST", body: JSON.stringify({ contentId }) });
}

export function deleteBookmark(contentId: string) {
  return apiRequest<void>(`/api/v1/bookmarks/${encodeURIComponent(contentId)}`, { method: "DELETE" });
}

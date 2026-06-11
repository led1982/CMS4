import { Acknowledgement, apiRequest, UserAcknowledgementItem } from "../../services/apiClient";

export function acknowledgeNotice(contentId: string) {
  return apiRequest<Acknowledgement>(`/api/v1/content/${contentId}/acknowledgements`, { method: "POST" });
}

export function listMyAcknowledgements(status?: "PENDING" | "COMPLETED") {
  const query = status ? `?status=${status}` : "";
  return apiRequest<UserAcknowledgementItem[]>(`/api/v1/me/acknowledgements${query}`);
}

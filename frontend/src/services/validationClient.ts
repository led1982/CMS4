import { apiRequest } from "./apiClient";
import type { ValidationReport } from "./scopeRequestClient";

export function validateScopeRequest(requestId: string) {
  return apiRequest<ValidationReport>(`/api/scope-requests/${requestId}/validate`, {
    method: "POST"
  });
}

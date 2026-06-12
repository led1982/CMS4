import { apiRequest } from "./apiClient";

export type ScopeRequestStatus = "draft" | "resolved" | "generated" | "validated" | "blocked";
export type ValidationStatus = "not_run" | "passed" | "passed_with_warnings" | "failed";
export type ArtifactType = "spec" | "plan" | "tasks";

export interface DomainDependencyInput {
  domainKey: string;
  requires: string[];
}

export interface ScopeRequestCreate {
  requestCode: string;
  slug: string;
  requestType: "new" | "change" | "fix";
  title: string;
  goal: string;
  selectedDomains: string[];
  autoAddedDomains: string[];
  declaredEffectiveDomains: string[];
  dependencies: DomainDependencyInput[];
  acceptanceCriteria: string[];
  sourceText?: string;
}

export interface ScopeRequest extends ScopeRequestCreate {
  id: string;
  status: ScopeRequestStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ScopeRequestSummary {
  id: string;
  requestCode: string;
  title: string;
  status: ScopeRequestStatus;
  effectiveDomainCount: number;
  artifactSetCount: number;
  validationStatus: ValidationStatus;
}

export interface Domain {
  domainKey: string;
  displayName: string;
  source: "selected" | "auto_added" | "declared_effective" | "default";
  requires: string[];
  generationStage: number;
  description?: string;
  status: "pending" | "planned" | "generated" | "validated" | "error";
}

export interface ArtifactSummary {
  artifactType: ArtifactType;
  path: string;
  status: "draft" | "generated" | "validated" | "incomplete" | "error";
}

export interface ArtifactSet {
  domainKey: string;
  status: "pending" | "complete" | "incomplete" | "validated" | "error";
  contentFingerprint?: string;
  generatedAt?: string;
  validatedAt?: string;
  artifacts: ArtifactSummary[];
}

export interface Artifact extends ArtifactSummary {
  title?: string;
  generatedFromTemplate?: string;
  contentFingerprint?: string;
  content: string;
}

export interface ValidationFinding {
  severity: "info" | "warning" | "error";
  findingType: string;
  domainKey: string;
  relatedDomainKey?: string;
  message: string;
  recommendedAction: string;
}

export interface ValidationReport {
  status: ValidationStatus;
  errorCount: number;
  warningCount: number;
  findings: ValidationFinding[];
}

export interface ScopeRequestDetail {
  request: ScopeRequest;
  domains: Domain[];
  artifactSets: ArtifactSet[];
  latestValidation: ValidationReport;
  assumptions: string[];
}

export function listScopeRequests(status?: ScopeRequestStatus) {
  const query = status ? `?status=${encodeURIComponent(status)}` : "";
  return apiRequest<{ items: ScopeRequestSummary[] }>(`/api/scope-requests${query}`);
}

export function createScopeRequest(input: ScopeRequestCreate) {
  return apiRequest<ScopeRequest>("/api/scope-requests", {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export function getScopeRequest(requestId: string) {
  return apiRequest<ScopeRequestDetail>(`/api/scope-requests/${requestId}`);
}

export function resolveDomains(requestId: string) {
  return apiRequest<{ requestId: string; domains: Domain[]; assumptions: string[] }>(`/api/scope-requests/${requestId}/resolve-domains`, {
    method: "POST"
  });
}

export function generateArtifacts(requestId: string) {
  return apiRequest<{ requestId: string; artifactSets: ArtifactSet[] }>(`/api/scope-requests/${requestId}/generate-artifacts`, {
    method: "POST",
    body: JSON.stringify({ forceRegenerate: false })
  });
}

export function getArtifact(requestId: string, domainKey: string, artifactType: ArtifactType) {
  return apiRequest<Artifact>(`/api/scope-requests/${requestId}/artifact-sets/${domainKey}/artifacts/${artifactType}`);
}

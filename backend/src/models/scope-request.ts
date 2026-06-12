export type ScopeRequestStatus = "draft" | "resolved" | "generated" | "validated" | "blocked";
export type RequestType = "new" | "change" | "fix";

export interface DomainDependencyInput {
  domainKey: string;
  requires: string[];
}

export interface ScopeRequestCreate {
  requestCode: string;
  slug: string;
  requestType: RequestType;
  title: string;
  goal: string;
  selectedDomains?: string[];
  autoAddedDomains?: string[];
  declaredEffectiveDomains?: string[];
  dependencies?: DomainDependencyInput[];
  acceptanceCriteria: string[];
  sourceText?: string;
}

export interface ScopeRequest extends Required<Omit<ScopeRequestCreate, "sourceText">> {
  id: string;
  sourceText: string;
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
  validationStatus: "not_run" | "passed" | "passed_with_warnings" | "failed";
}

export function normalizeScopeRequest(input: ScopeRequestCreate): ScopeRequestCreate {
  return {
    requestCode: String(input.requestCode ?? "").trim(),
    slug: String(input.slug ?? "").trim(),
    requestType: input.requestType,
    title: String(input.title ?? "").trim(),
    goal: String(input.goal ?? "").trim(),
    selectedDomains: normalizeDomainKeys(input.selectedDomains ?? []),
    autoAddedDomains: normalizeDomainKeys(input.autoAddedDomains ?? []),
    declaredEffectiveDomains: normalizeDomainKeys(input.declaredEffectiveDomains ?? []),
    dependencies: (input.dependencies ?? []).map((dependency) => ({
      domainKey: normalizeDomainKey(dependency.domainKey),
      requires: normalizeDomainKeys(dependency.requires ?? [])
    })),
    acceptanceCriteria: (input.acceptanceCriteria ?? []).map((criterion) => criterion.trim()).filter(Boolean),
    sourceText: input.sourceText?.trim() ?? ""
  };
}

export function normalizeDomainKeys(values: string[]): string[] {
  return values.map(normalizeDomainKey).filter(Boolean);
}

export function normalizeDomainKey(value: string): string {
  return String(value ?? "").trim().toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "");
}

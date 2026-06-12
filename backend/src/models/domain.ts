export type DomainSource = "selected" | "auto_added" | "declared_effective" | "default";
export type DomainStatus = "pending" | "planned" | "generated" | "validated" | "error";
export type GenerationStageStatus = "pending" | "ready" | "generated" | "validated" | "blocked";

export interface Domain {
  id: string;
  scopeRequestId: string;
  domainKey: string;
  displayName: string;
  source: DomainSource;
  requires: string[];
  generationStage: number;
  description: string;
  status: DomainStatus;
}

export interface GenerationStage {
  scopeRequestId: string;
  stageNumber: number;
  domainKeys: string[];
  status: GenerationStageStatus;
  validationSummary?: {
    errorCount: number;
    warningCount: number;
  };
}

export interface GenerationOrder {
  stages: Array<{
    stageNumber: number;
    domainKeys: string[];
    status: GenerationStageStatus;
  }>;
}

export function displayNameForDomain(domainKey: string): string {
  return domainKey
    .split("-")
    .filter(Boolean)
    .map((part) => (part.toLowerCase() === "cms" ? "CMS" : part.charAt(0).toUpperCase() + part.slice(1)))
    .join(" ");
}

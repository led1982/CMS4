export type ValidationSeverity = "info" | "warning" | "error";
export type ValidationStatus = "not_run" | "passed" | "passed_with_warnings" | "failed";
export type ValidationFindingType =
  | "missing_artifact"
  | "missing_dependency"
  | "later_stage_dependency"
  | "self_dependency"
  | "cycle"
  | "duplicate_domain"
  | "assumption_applied";

export interface ValidationFinding {
  severity: ValidationSeverity;
  findingType: ValidationFindingType;
  domainKey: string;
  relatedDomainKey?: string;
  message: string;
  recommendedAction: string;
}

export interface ValidationRun {
  id: string;
  scopeRequestId: string;
  startedAt: string;
  completedAt: string;
  status: Exclude<ValidationStatus, "not_run">;
  errorCount: number;
  warningCount: number;
  findings: ValidationFinding[];
}

export interface ValidationReport {
  status: ValidationStatus;
  errorCount: number;
  warningCount: number;
  findings: ValidationFinding[];
}

export const emptyValidationReport: ValidationReport = {
  status: "not_run",
  errorCount: 0,
  warningCount: 0,
  findings: []
};

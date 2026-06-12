import { randomUUID } from "node:crypto";
import { ValidationFinding, ValidationReport, ValidationRun, ValidationStatus, emptyValidationReport } from "../models/validation-finding.js";

export interface ValidationRepository {
  saveRun(scopeRequestId: string, report: ValidationReport): ValidationRun;
  latestReport(scopeRequestId: string): ValidationReport;
  listRuns(scopeRequestId: string): ValidationRun[];
}

export class InMemoryValidationRepository implements ValidationRepository {
  private readonly runs = new Map<string, ValidationRun[]>();

  saveRun(scopeRequestId: string, report: ValidationReport): ValidationRun {
    const now = new Date().toISOString();
    const status = report.status === "not_run" ? statusFromFindings(report.findings) : report.status;
    const run: ValidationRun = {
      id: randomUUID(),
      scopeRequestId,
      startedAt: now,
      completedAt: now,
      status,
      errorCount: report.errorCount,
      warningCount: report.warningCount,
      findings: report.findings
    };
    const current = this.runs.get(scopeRequestId) ?? [];
    this.runs.set(scopeRequestId, [...current, run]);
    return run;
  }

  latestReport(scopeRequestId: string): ValidationReport {
    const latest = this.listRuns(scopeRequestId).at(-1);
    if (!latest) {
      return emptyValidationReport;
    }
    return {
      status: latest.status,
      errorCount: latest.errorCount,
      warningCount: latest.warningCount,
      findings: latest.findings
    };
  }

  listRuns(scopeRequestId: string): ValidationRun[] {
    return [...(this.runs.get(scopeRequestId) ?? [])];
  }
}

export function reportFromFindings(findings: ValidationFinding[]): ValidationReport {
  const errorCount = findings.filter((finding) => finding.severity === "error").length;
  const warningCount = findings.filter((finding) => finding.severity === "warning").length;
  return {
    status: statusFromCounts(errorCount, warningCount),
    errorCount,
    warningCount,
    findings
  };
}

function statusFromFindings(findings: ValidationFinding[]): Exclude<ValidationStatus, "not_run"> {
  const report = reportFromFindings(findings);
  return report.status === "not_run" ? "passed" : report.status;
}

function statusFromCounts(errorCount: number, warningCount: number): Exclude<ValidationStatus, "not_run"> {
  if (errorCount > 0) {
    return "failed";
  }
  if (warningCount > 0) {
    return "passed_with_warnings";
  }
  return "passed";
}

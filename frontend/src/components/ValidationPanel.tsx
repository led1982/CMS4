import type { ValidationReport } from "../services/scopeRequestClient";

export function ValidationPanel({ report }: { report: ValidationReport }) {
  return (
    <section>
      <div className="section-heading">
        <h2>Validation</h2>
        <span className={`status-pill status-${report.status}`}>{report.status.replaceAll("_", " ")}</span>
      </div>
      <p className="muted-text">
        {report.errorCount} errors, {report.warningCount} warnings
      </p>
      {report.findings.length === 0 ? (
        <p>No validation findings.</p>
      ) : (
        <div className="scope-table validation-table" role="table" aria-label="Validation findings">
          <div className="scope-table-row scope-table-head" role="row">
            <span>Severity</span>
            <span>Type</span>
            <span>Domain</span>
            <span>Action</span>
          </div>
          {report.findings.map((finding) => (
            <div className="scope-table-row" role="row" key={`${finding.findingType}-${finding.domainKey}-${finding.relatedDomainKey ?? finding.message}`}>
              <span className={`status-pill status-${finding.severity}`}>{finding.severity}</span>
              <span>{finding.findingType}</span>
              <span>{finding.relatedDomainKey ? `${finding.domainKey} -> ${finding.relatedDomainKey}` : finding.domainKey}</span>
              <span>{finding.recommendedAction}</span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

import { Link } from "react-router-dom";
import type { ArtifactSet, ArtifactType } from "../services/scopeRequestClient";
import { formatDate } from "../services/apiClient";

const artifactLabels: Record<ArtifactType, string> = {
  spec: "spec.md",
  plan: "plan.md",
  tasks: "tasks.md"
};

export function ArtifactStatusTable({ artifactSets, requestId }: { artifactSets: ArtifactSet[]; requestId: string }) {
  return (
    <section>
      <div className="section-heading">
        <h2>Artifacts</h2>
      </div>
      <div className="scope-table artifact-table" role="table" aria-label="Artifact status">
        <div className="scope-table-row scope-table-head" role="row">
          <span>Domain</span>
          <span>Artifact</span>
          <span>Status</span>
          <span>Generated</span>
          <span>Preview</span>
        </div>
        {artifactSets.flatMap((artifactSet) =>
          artifactSet.artifacts.map((artifact) => (
            <div className="scope-table-row" role="row" key={`${artifactSet.domainKey}-${artifact.artifactType}`}>
              <span>{artifactSet.domainKey}</span>
              <span>{artifactLabels[artifact.artifactType]}</span>
              <span className={`status-pill status-${artifact.status}`}>{artifact.status}</span>
              <span>{formatDate(artifactSet.generatedAt)}</span>
              <Link to={`/scope-requests/${requestId}/artifacts/${artifactSet.domainKey}?type=${artifact.artifactType}`}>Open</Link>
            </div>
          ))
        )}
      </div>
    </section>
  );
}

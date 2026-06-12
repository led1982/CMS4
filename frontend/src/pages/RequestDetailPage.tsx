import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArtifactStatusTable } from "../components/ArtifactStatusTable";
import { AssumptionsPanel } from "../components/AssumptionsPanel";
import { DomainMatrix } from "../components/DomainMatrix";
import { GenerationOrderTimeline } from "../components/GenerationOrderTimeline";
import { EmptyState, ErrorState, LoadingPanel } from "../components/StatePanel";
import { ValidationPanel } from "../components/ValidationPanel";
import { formatDate, useApi } from "../services/apiClient";
import { generateArtifacts, getScopeRequest, resolveDomains } from "../services/scopeRequestClient";
import { validateScopeRequest } from "../services/validationClient";

export function RequestDetailPage() {
  const { requestId = "" } = useParams();
  const [refreshKey, setRefreshKey] = useState(0);
  const [actionError, setActionError] = useState<string | null>(null);
  const { data, error, loading } = useApi(() => getScopeRequest(requestId), [requestId, refreshKey]);

  async function runAction(action: () => Promise<unknown>) {
    setActionError(null);
    try {
      await action();
      setRefreshKey((current) => current + 1);
    } catch (reason) {
      setActionError(reason instanceof Error ? reason.message : "Action failed.");
    }
  }

  if (loading) return <LoadingPanel label="Loading request detail" />;
  if (error) return <ErrorState error={error} />;
  if (!data) return <EmptyState title="Request detail is unavailable" />;

  const firstArtifactSet = data.artifactSets[0];

  return (
    <div className="scope-layout">
      <section className="section-full">
        <div className="section-heading">
          <div>
            <h1>
              {data.request.requestCode} / {data.request.title}
            </h1>
            <p className="muted-text">Goal: {data.request.goal}</p>
          </div>
          <div className="button-group">
            <button type="button" className="secondary" onClick={() => runAction(() => resolveDomains(requestId))}>
              Resolve Domains
            </button>
            <button type="button" onClick={() => runAction(() => generateArtifacts(requestId))}>
              Generate
            </button>
            <button type="button" onClick={() => runAction(() => validateScopeRequest(requestId))}>
              Validate
            </button>
            {firstArtifactSet && <Link className="button-link" to={`/scope-requests/${requestId}/artifacts/${firstArtifactSet.domainKey}`}>Preview</Link>}
          </div>
        </div>
        {actionError && (
          <div className="state-panel state-panel-error" role="alert">
            {actionError}
          </div>
        )}
        <div className="summary-strip">
          <span className={`status-pill status-${data.request.status}`}>{data.request.status}</span>
          <span>{data.domains.length} domains</span>
          <span>{data.artifactSets.length} artifact sets</span>
          <span>Updated {formatDate(data.request.updatedAt)}</span>
        </div>
      </section>

      <AssumptionsPanel assumptions={data.assumptions} />
      <GenerationOrderTimeline domains={data.domains} />
      <DomainMatrix domains={data.domains} />
      <ArtifactStatusTable artifactSets={data.artifactSets} requestId={requestId} />
      <ValidationPanel report={data.latestValidation} />
    </div>
  );
}

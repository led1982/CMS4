import { useMemo } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import { MarkdownPreview } from "../components/MarkdownPreview";
import { EmptyState, ErrorState, LoadingPanel } from "../components/StatePanel";
import { formatDate, useApi } from "../services/apiClient";
import { ArtifactType, getArtifact, getScopeRequest } from "../services/scopeRequestClient";

const artifactTypes: ArtifactType[] = ["spec", "plan", "tasks"];

export function ArtifactPreviewPage() {
  const { requestId = "", domainKey = "" } = useParams();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const requestedType = params.get("type") as ArtifactType | null;
  const selectedType = requestedType && artifactTypes.includes(requestedType) ? requestedType : "spec";
  const { data: detail } = useApi(() => getScopeRequest(requestId), [requestId]);
  const activeDomain = domainKey || detail?.domains[0]?.domainKey || "";
  const { data, error, loading } = useApi(() => getArtifact(requestId, activeDomain, selectedType), [requestId, activeDomain, selectedType]);

  const domainOptions = useMemo(() => detail?.domains ?? [], [detail?.domains]);

  if (!activeDomain) return <EmptyState title="No generated domain is available for preview" />;
  if (loading) return <LoadingPanel label="Loading artifact preview" />;
  if (error) return <ErrorState error={error} />;
  if (!data) return <EmptyState title="Artifact is unavailable" />;

  return (
    <div className="scope-layout">
      <section className="section-full">
        <div className="section-heading">
          <div>
            <h1>Artifact Preview</h1>
            <p className="muted-text">
              {activeDomain} / {data.title ?? `${selectedType}.md`}
            </p>
          </div>
          <Link className="button-link secondary" to={`/scope-requests/${requestId}`}>
            Back to Detail
          </Link>
        </div>
        <div className="preview-toolbar">
          <label className="field-inline">
            <span>Domain</span>
            <select
              value={activeDomain}
              onChange={(event) => {
                navigate(`/scope-requests/${requestId}/artifacts/${event.target.value}?type=${selectedType}`);
              }}
            >
              {domainOptions.map((domain) => (
                <option value={domain.domainKey} key={domain.domainKey}>
                  {domain.domainKey}
                </option>
              ))}
            </select>
          </label>
          <div className="tabs" role="tablist" aria-label="Artifact type">
            {artifactTypes.map((artifactType) => (
              <button
                type="button"
                role="tab"
                aria-selected={artifactType === selectedType}
                className={artifactType === selectedType ? "active" : "secondary"}
                key={artifactType}
                onClick={() => setParams({ type: artifactType })}
              >
                {artifactType}.md
              </button>
            ))}
          </div>
        </div>
      </section>

      <section className="section-full artifact-preview">
        <MarkdownPreview content={data.content} />
      </section>

      <section className="section-full metadata-panel">
        <strong>Status:</strong> {data.status} <strong>Template:</strong> {data.generatedFromTemplate ?? "-"} <strong>Generated:</strong> {formatDate(new Date().toISOString())}
      </section>
    </div>
  );
}

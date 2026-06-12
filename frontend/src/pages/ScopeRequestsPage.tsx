import { Link, useSearchParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../components/StatePanel";
import { formatDate, useApi } from "../services/apiClient";
import { listScopeRequests, ScopeRequestStatus } from "../services/scopeRequestClient";

const statuses: Array<ScopeRequestStatus | ""> = ["", "draft", "resolved", "generated", "validated", "blocked"];

export function ScopeRequestsPage() {
  const [params, setParams] = useSearchParams();
  const status = (params.get("status") ?? "") as ScopeRequestStatus | "";
  const { data, error, loading } = useApi(() => listScopeRequests(status || undefined), [status]);

  if (loading) return <LoadingPanel label="Loading scope requests" />;
  if (error) return <ErrorState error={error} />;

  return (
    <div className="scope-layout">
      <section className="section-full">
        <div className="section-heading">
          <div>
            <h1>CMS Scope Requests</h1>
            <p className="muted-text">Review generated scope artifacts, dependency validation, and assumptions.</p>
          </div>
          <Link className="button-link" to="/scope-requests/import">
            Import Request
          </Link>
        </div>
        <label className="field-inline">
          <span>Status</span>
          <select
            value={status}
            onChange={(event) => {
              const next = event.target.value;
              setParams(next ? { status: next } : {});
            }}
          >
            {statuses.map((item) => (
              <option value={item} key={item || "all"}>
                {item || "all"}
              </option>
            ))}
          </select>
        </label>
      </section>

      <section className="section-full">
        {!data || data.items.length === 0 ? (
          <EmptyState title="No scope requests yet" action={<Link to="/scope-requests/import">Import CMS4-4</Link>} />
        ) : (
          <div className="scope-table" role="table" aria-label="Scope request summary">
            <div className="scope-table-row scope-table-head" role="row">
              <span>Code</span>
              <span>Title</span>
              <span>Status</span>
              <span>Domains</span>
              <span>Artifacts</span>
              <span>Validation</span>
            </div>
            {data.items.map((item) => (
              <Link className="scope-table-row scope-table-link" role="row" to={`/scope-requests/${item.id}`} key={item.id}>
                <span>{item.requestCode}</span>
                <span>{item.title}</span>
                <span className={`status-pill status-${item.status}`}>{item.status}</span>
                <span>{item.effectiveDomainCount}</span>
                <span>{item.artifactSetCount}</span>
                <span className={`status-pill status-${item.validationStatus}`}>{item.validationStatus.replaceAll("_", " ")}</span>
              </Link>
            ))}
          </div>
        )}
        <p className="muted-text scope-footnote">Last refreshed {formatDate(new Date().toISOString())}</p>
      </section>
    </div>
  );
}

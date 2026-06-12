import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { StatusBadge } from "../../components/StatusBadge";
import { formatDate, useApi } from "../../services/apiClient";
import type { ContentSummary } from "../../services/apiClient";
import { getContentHealth } from "./reportsApi";

export function ContentHealthPage() {
  const { data, error, loading } = useApi(getContentHealth, []);

  if (loading) return <LoadingPanel label="Loading content health" />;
  if (error) return <ErrorState error={error} />;
  if (!data) return <EmptyState title="Content health is unavailable" />;

  return (
    <section>
      <h1>Content Health</h1>
      <div className="metric-grid">
        <div>
          <strong>{data.stale.length}</strong>
          <span>Stale</span>
        </div>
        <div>
          <strong>{data.expiring.length}</strong>
          <span>Expiring</span>
        </div>
        <div>
          <strong>{data.archived.length}</strong>
          <span>Archived</span>
        </div>
        <div>
          <strong>{data.recentlyPublished.length}</strong>
          <span>Recently published</span>
        </div>
      </div>
      <HealthList title="Expiring Content" items={data.expiring} />
      <HealthList title="Stale Content" items={data.stale} />
    </section>
  );
}

function HealthList({ title, items }: { title: string; items: ContentSummary[] }) {
  return (
    <>
      <h2>{title}</h2>
      {items.length === 0 ? (
        <EmptyState title="No items" />
      ) : (
        <div className="table-list">
          {items.map((item) => (
            <div className="table-row" key={item.id}>
              <span>{item.title}</span>
              <span>{item.category.name}</span>
              <StatusBadge status={item.status} />
              <span>{formatDate(item.updatedAt)}</span>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

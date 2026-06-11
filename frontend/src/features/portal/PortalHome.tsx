import { Link } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { PriorityBadge, StatusBadge } from "../../components/StatusBadge";
import { formatDate, useApi } from "../../services/apiClient";
import { getPortalHome } from "./portalApi";

export function PortalHome() {
  const { data, error, loading } = useApi(getPortalHome, []);

  if (loading) return <LoadingPanel label="Loading portal home" />;
  if (error) return <ErrorState error={error} />;
  if (!data) return <EmptyState title="Portal content is unavailable" />;

  return (
    <div className="page-grid portal-home">
      <section className="section-full">
        <div className="section-heading">
          <h1>Portal Home</h1>
          <Link className="button-link" to="/search">
            Search
          </Link>
        </div>
        <div className="notice-strip">
          {data.requiredNotices.length === 0 ? (
            <EmptyState title="No required notices are pending" />
          ) : (
            data.requiredNotices.map((item) => (
              <article className="content-row content-row-urgent" key={item.id}>
                <div>
                  <Link to={`/content/${item.id}`}>{item.title}</Link>
                  <p>{item.summary}</p>
                </div>
                <div className="row-meta">
                  <PriorityBadge priority={item.priority} />
                  <span>{item.acknowledgementStatus}</span>
                </div>
              </article>
            ))
          )}
        </div>
      </section>

      <section>
        <h2>Pinned Content</h2>
        <div className="list-stack">
          {data.pinnedContent.map((item) => (
            <article className="content-row" key={item.id}>
              <div>
                <Link to={`/content/${item.id}`}>{item.title}</Link>
                <p>{item.category.name} · {formatDate(item.updatedAt)}</p>
              </div>
              <PriorityBadge priority={item.priority} />
            </article>
          ))}
        </div>
      </section>

      <section>
        <h2>Categories</h2>
        <div className="category-list">
          {data.categories.map((category) => (
            <Link key={category.id} to={`/search?categoryId=${category.id}`}>
              {category.name}
            </Link>
          ))}
        </div>
      </section>

      <section className="section-full">
        <h2>Recently Updated</h2>
        <div className="table-list">
          {data.recentContent.map((item) => (
            <Link className="table-row" to={`/content/${item.id}`} key={item.id}>
              <span>{item.title}</span>
              <span>{item.contentType.replace("_", " ")}</span>
              <span>{item.category.name}</span>
              <StatusBadge status={item.status} />
              <span>{formatDate(item.updatedAt)}</span>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}

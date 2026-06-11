import { Link } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { StatusBadge } from "../../components/StatusBadge";
import { formatDate, useApi } from "../../services/apiClient";
import { searchContent } from "../portal/portalApi";

export function AuthorWorkspace() {
  const { data, error, loading } = useApi(() => searchContent({ status: "DRAFT", size: 100 }), []);

  if (loading) return <LoadingPanel label="Loading author workspace" />;
  if (error) return <ErrorState error={error} />;

  return (
    <section>
      <div className="section-heading">
        <h1>Author Workspace</h1>
        <Link className="button-link" to="/author/new">
          New Content
        </Link>
      </div>
      {!data || data.items.length === 0 ? (
        <EmptyState title="No editable drafts" action={<Link to="/author/new">Create content</Link>} />
      ) : (
        <div className="table-list">
          {data.items.map((item) => (
            <Link className="table-row" to={`/author/${item.id}`} key={item.id}>
              <span>{item.title}</span>
              <span>{item.category.name}</span>
              <StatusBadge status={item.status} />
              <span>{formatDate(item.updatedAt)}</span>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
}

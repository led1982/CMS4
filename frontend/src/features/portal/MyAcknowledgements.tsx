import { Link } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { formatDate, useApi } from "../../services/apiClient";
import { listMyAcknowledgements } from "./acknowledgementApi";

export function MyAcknowledgements() {
  const { data, error, loading } = useApi(() => listMyAcknowledgements(), []);

  if (loading) return <LoadingPanel label="Loading acknowledgements" />;
  if (error) return <ErrorState error={error} />;
  if (!data || data.length === 0) return <EmptyState title="No acknowledgement items" />;

  return (
    <section>
      <h1>My Acknowledgements</h1>
      <div className="table-list">
        {data.map((item) => (
          <Link className="table-row" to={`/content/${item.content.id}`} key={item.content.id}>
            <span>{item.content.title}</span>
            <span>{item.status}</span>
            <span>Due {formatDate(item.dueAt)}</span>
            <span>{item.acknowledgedAt ? `Completed ${formatDate(item.acknowledgedAt)}` : "Pending"}</span>
          </Link>
        ))}
      </div>
    </section>
  );
}

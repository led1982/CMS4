import { FormEvent, useState } from "react";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { formatDate, useApi } from "../../services/apiClient";
import { searchAuditEvents } from "./adminApi";

export function AuditLog() {
  const [action, setAction] = useState("");
  const [activeAction, setActiveAction] = useState("");
  const events = useApi(() => searchAuditEvents({ action: activeAction || undefined }), [activeAction]);

  function submit(event: FormEvent) {
    event.preventDefault();
    setActiveAction(action);
  }

  return (
    <div className="page-layout with-sidebar">
      <aside className="side-panel admin-nav">
        <a href="/admin/taxonomy">Taxonomy</a>
        <a href="/admin/access">Access</a>
        <a href="/admin/audit">Audit</a>
      </aside>
      <section>
        <h1>Audit Log</h1>
        <form className="filter-form horizontal" onSubmit={submit}>
          <label>
            Action
            <input value={action} onChange={(event) => setAction(event.target.value)} />
          </label>
          <button type="submit">Filter</button>
        </form>
        {events.loading && <LoadingPanel label="Loading audit events" />}
        {events.error && <ErrorState error={events.error} />}
        {events.data?.items.length === 0 && <EmptyState title="No audit events" />}
        <div className="table-list">
          {events.data?.items.map((event) => (
            <div className="table-row" key={event.id}>
              <span>{formatDate(event.occurredAt)}</span>
              <span>{event.actor.displayName}</span>
              <span>{event.action}</span>
              <span>{event.targetType}</span>
              <span>{event.summary}</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

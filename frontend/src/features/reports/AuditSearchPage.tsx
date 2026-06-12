import { FormEvent, useState } from "react";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { formatDate, useApi } from "../../services/apiClient";
import { searchAuditEvents } from "./reportsApi";

export function AuditSearchPage() {
  const [action, setAction] = useState("");
  const [activeAction, setActiveAction] = useState("");
  const events = useApi(() => searchAuditEvents({ action: activeAction || undefined }), [activeAction]);

  function submit(event: FormEvent) {
    event.preventDefault();
    setActiveAction(action);
  }

  return (
    <section>
      <h1>Audit Search</h1>
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
  );
}

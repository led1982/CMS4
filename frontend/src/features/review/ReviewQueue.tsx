import { useState } from "react";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { StatusBadge } from "../../components/StatusBadge";
import { ContentDetail, ContentSummary, useApi } from "../../services/apiClient";
import { getContent } from "../portal/portalApi";
import { getReviewQueue, publishContent, reviewContent } from "../authoring/contentWorkflowApi";

export function ReviewQueue() {
  const [selected, setSelected] = useState<ContentSummary | null>(null);
  const [decisionMessage, setDecisionMessage] = useState<string | null>(null);
  const queue = useApi(getReviewQueue, [decisionMessage]);
  const detail = useApi<ContentDetail | null>(() => (selected ? getContent(selected.id) : Promise.resolve(null)), [selected?.id, decisionMessage]);

  async function decide(decision: "APPROVE" | "REJECT" | "REQUEST_CHANGES") {
    if (!detail.data) return;
    const updated = await reviewContent(detail.data.id, detail.data.versionToken, decision, decision === "APPROVE" ? "Approved" : "Needs revision");
    setDecisionMessage(`${updated.title}: ${updated.workflow?.latestDecision}`);
  }

  async function publish() {
    if (!detail.data) return;
    const updated = await publishContent(detail.data.id, detail.data.versionToken);
    setDecisionMessage(`${updated.title}: published`);
  }

  return (
    <div className="page-layout with-sidebar">
      <aside className="side-panel">
        <h1>Review Queue</h1>
        {queue.loading && <LoadingPanel label="Loading queue" />}
        {queue.error && <ErrorState error={queue.error} />}
        {queue.data?.length === 0 && <EmptyState title="No review items" />}
        {queue.data?.map((item) => (
          <button className="queue-item" key={item.id} onClick={() => setSelected(item)}>
            <span>{item.title}</span>
            <StatusBadge status={item.status} />
          </button>
        ))}
      </aside>
      <section>
        {decisionMessage && <p className="inline-success">{decisionMessage}</p>}
        {detail.loading && selected && <LoadingPanel label="Loading review detail" />}
        {detail.error && <ErrorState error={detail.error} />}
        {!selected && <EmptyState title="Select a submission" />}
        {detail.data && (
          <article className="review-detail">
            <h2>{detail.data.title}</h2>
            <p>{detail.data.summary}</p>
            <dl className="metadata-grid">
              <dt>Category</dt>
              <dd>{detail.data.category.name}</dd>
              <dt>Audience</dt>
              <dd>{detail.data.audience.name}</dd>
              <dt>Author</dt>
              <dd>{detail.data.author?.displayName}</dd>
              <dt>Attachments</dt>
              <dd>{detail.data.attachments.length}</dd>
            </dl>
            <div className="button-group">
              <button onClick={() => decide("APPROVE")}>Approve</button>
              <button onClick={() => decide("REQUEST_CHANGES")}>Request Changes</button>
              <button onClick={() => decide("REJECT")}>Reject</button>
              {detail.data.workflow?.latestDecision === "APPROVED" && <button onClick={publish}>Publish</button>}
            </div>
          </article>
        )}
      </section>
    </div>
  );
}

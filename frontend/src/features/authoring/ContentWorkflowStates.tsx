import { ContentDetail } from "../../services/apiClient";
import { StatusBadge } from "../../components/StatusBadge";

export function ContentWorkflowStates({ content }: { content: ContentDetail }) {
  return (
    <aside className="workflow-panel">
      <h2>Workflow</h2>
      <dl>
        <dt>Status</dt>
        <dd>
          <StatusBadge status={content.status} />
        </dd>
        <dt>Owner</dt>
        <dd>{content.owner.displayName}</dd>
        <dt>Audience</dt>
        <dd>{content.audience.name}</dd>
        <dt>Revision</dt>
        <dd>{content.revisionNumber}</dd>
        <dt>Latest decision</dt>
        <dd>{content.workflow?.latestDecision ?? "None"}</dd>
        <dt>Comments</dt>
        <dd>{content.workflow?.latestComments ?? "-"}</dd>
      </dl>
    </aside>
  );
}

import { ContentStatus, Priority } from "../services/apiClient";

const statusLabels: Record<ContentStatus, string> = {
  DRAFT: "Draft",
  IN_REVIEW: "In review",
  REJECTED: "Rejected",
  SCHEDULED: "Scheduled",
  PUBLISHED: "Published",
  EXPIRED: "Expired",
  ARCHIVED: "Archived",
  DELETED: "Deleted"
};

export function StatusBadge({ status }: { status: ContentStatus }) {
  return <span className={`badge status status-${status.toLowerCase().replace("_", "-")}`}>{statusLabels[status]}</span>;
}

export function PriorityBadge({ priority }: { priority: Priority }) {
  return <span className={`badge priority priority-${priority.toLowerCase()}`}>{priority === "NORMAL" ? "Normal" : priority === "PINNED" ? "Pinned" : "Urgent"}</span>;
}

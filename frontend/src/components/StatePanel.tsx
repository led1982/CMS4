import { ApiError } from "../services/apiClient";
import type { ReactNode } from "react";

export function LoadingPanel({ label = "Loading" }: { label?: string }) {
  return (
    <div className="state-panel" role="status" aria-live="polite">
      {label}
    </div>
  );
}

export function EmptyState({ title, action }: { title: string; action?: ReactNode }) {
  return (
    <div className="state-panel">
      <strong>{title}</strong>
      {action}
    </div>
  );
}

export function ErrorState({ error }: { error: Error | ApiError }) {
  const detail = error instanceof ApiError ? `${error.payload.code}: ${error.payload.message}` : error.message;
  return (
    <div className="state-panel state-panel-error" role="alert">
      {detail}
    </div>
  );
}

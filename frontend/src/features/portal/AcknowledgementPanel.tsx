import { useState } from "react";
import { ApiError, ContentDetail } from "../../services/apiClient";
import { acknowledgeNotice } from "./acknowledgementApi";

export function AcknowledgementPanel({ content, onAcknowledged }: { content: ContentDetail; onAcknowledged: () => void }) {
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  if (!content.requiresAcknowledgement) {
    return null;
  }

  async function acknowledge() {
    setSaving(true);
    setError(null);
    try {
      await acknowledgeNotice(content.id);
      onAcknowledged();
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.payload.message : "Unable to acknowledge notice");
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="action-panel">
      <div>
        <h2>Acknowledgement</h2>
        <p>Status: {content.acknowledgementStatus}</p>
      </div>
      {content.acknowledgementStatus !== "COMPLETED" && (
        <button onClick={acknowledge} disabled={saving}>
          {saving ? "Recording" : "Acknowledge"}
        </button>
      )}
      {error && <p className="inline-error">{error}</p>}
    </section>
  );
}

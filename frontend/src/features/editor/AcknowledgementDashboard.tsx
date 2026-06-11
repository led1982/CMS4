import { useEffect, useState } from "react";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { formatDate, useApi } from "../../services/apiClient";
import { getAcknowledgementReport, searchContent } from "../portal/portalApi";

export function AcknowledgementDashboard() {
  const notices = useApi(() => searchContent({ contentType: "NOTICE", acknowledgementRequired: true, size: 20 }), []);
  const [contentId, setContentId] = useState("");

  useEffect(() => {
    if (!contentId && notices.data?.items[0]) {
      setContentId(notices.data.items[0].id);
    }
  }, [notices.data, contentId]);

  const report = useApi(() => (contentId ? getAcknowledgementReport(contentId) : Promise.resolve(null)), [contentId]);

  if (notices.loading) return <LoadingPanel label="Loading acknowledgement dashboard" />;
  if (notices.error) return <ErrorState error={notices.error} />;
  if (!notices.data || notices.data.items.length === 0) return <EmptyState title="No acknowledgement-required notices" />;

  return (
    <section>
      <h1>Acknowledgement Dashboard</h1>
      <label>
        Notice
        <select value={contentId} onChange={(event) => setContentId(event.target.value)}>
          {notices.data.items.map((item) => (
            <option key={item.id} value={item.id}>
              {item.title}
            </option>
          ))}
        </select>
      </label>
      {report.loading && <LoadingPanel label="Loading report" />}
      {report.error && <ErrorState error={report.error} />}
      {report.data && (
        <>
          <div className="metric-grid">
            <div>
              <strong>{report.data.targetedCount}</strong>
              <span>Targeted</span>
            </div>
            <div>
              <strong>{report.data.acknowledgedCount}</strong>
              <span>Completed</span>
            </div>
            <div>
              <strong>{report.data.pendingCount}</strong>
              <span>Pending</span>
            </div>
            <div>
              <strong>{Math.round(report.data.completionRate * 100)}%</strong>
              <span>Complete</span>
            </div>
          </div>
          <div className="table-list">
            {report.data.items.map((item) => (
              <div className="table-row" key={item.user.id}>
                <span>{item.user.displayName}</span>
                <span>{item.department}</span>
                <span>{item.status}</span>
                <span>{formatDate(item.acknowledgedAt)}</span>
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  );
}

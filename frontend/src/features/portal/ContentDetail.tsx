import { useState } from "react";
import { marked } from "marked";
import { Link, useParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { PriorityBadge, StatusBadge } from "../../components/StatusBadge";
import { formatDate, useApi } from "../../services/apiClient";
import { AcknowledgementPanel } from "./AcknowledgementPanel";
import { getAttachmentDownload, getContent } from "./portalApi";

export function ContentDetail() {
  const { contentId = "" } = useParams();
  const [refresh, setRefresh] = useState(0);
  const { data, error, loading } = useApi(() => getContent(contentId), [contentId, refresh]);

  if (loading) return <LoadingPanel label="Loading content" />;
  if (error) return <ErrorState error={error} />;
  if (!data) return <EmptyState title="Content is unavailable" />;

  const html = marked.parse(data.body ?? "") as string;

  async function downloadAttachment(attachmentId: string) {
    const download = await getAttachmentDownload(data.id, attachmentId);
    window.location.assign(download.downloadUrl);
  }

  return (
    <article className="content-detail">
      <header className="detail-header">
        <div>
          <Link to="/search">Search results</Link>
          <h1>{data.title}</h1>
          <p>{data.summary}</p>
          <div className="meta-line">
            <StatusBadge status={data.status} />
            <PriorityBadge priority={data.priority} />
            <span>{data.category.name}</span>
            <span>Updated {formatDate(data.updatedAt)}</span>
          </div>
        </div>
      </header>

      <AcknowledgementPanel content={data} onAcknowledged={() => setRefresh((value) => value + 1)} />

      <section className="markdown-body" dangerouslySetInnerHTML={{ __html: html }} />

      <section>
        <h2>Attachments</h2>
        {data.attachments.length === 0 ? (
          <EmptyState title="No attachments" />
        ) : (
          <div className="table-list">
            {data.attachments.map((attachment) => (
              <button className="table-row as-button" key={attachment.id} onClick={() => downloadAttachment(attachment.id)}>
                <span>{attachment.fileName}</span>
                <span>{attachment.mimeType}</span>
                <span>{Math.round(attachment.sizeBytes / 1024)} KB</span>
                <span>{attachment.validationStatus}</span>
              </button>
            ))}
          </div>
        )}
      </section>
    </article>
  );
}

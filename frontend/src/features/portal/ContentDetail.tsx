import { useState } from "react";
import { marked } from "marked";
import { Link, useParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { PriorityBadge, StatusBadge } from "../../components/StatusBadge";
import { formatDate, useApi } from "../../services/apiClient";
import { AcknowledgementPanel } from "./AcknowledgementPanel";
import { createBookmark, deleteBookmark, getAttachmentDownload, getContent } from "./portalApi";

export function ContentDetail() {
  const { contentId = "" } = useParams();
  const [refresh, setRefresh] = useState(0);
  const [bookmarked, setBookmarked] = useState(false);
  const [bookmarkMessage, setBookmarkMessage] = useState<string | null>(null);
  const { data, error, loading } = useApi(() => getContent(contentId), [contentId, refresh]);

  if (loading) return <LoadingPanel label="Loading content" />;
  if (error) return <ErrorState error={error} />;
  if (!data) return <EmptyState title="Content is unavailable" />;

  const content = data;
  const html = marked.parse(content.body ?? "") as string;

  async function downloadAttachment(attachmentId: string) {
    const download = await getAttachmentDownload(content.id, attachmentId);
    window.location.assign(download.downloadUrl);
  }

  async function toggleBookmark() {
    if (bookmarked) {
      await deleteBookmark(content.id);
      setBookmarked(false);
      setBookmarkMessage("Bookmark removed");
    } else {
      await createBookmark(content.id);
      setBookmarked(true);
      setBookmarkMessage("Bookmarked");
    }
  }

  return (
    <article className="content-detail">
      <header className="detail-header">
        <div>
          <Link to="/search">Search results</Link>
          <h1>{content.title}</h1>
          <p>{content.summary}</p>
          <div className="meta-line">
            <StatusBadge status={content.status} />
            <PriorityBadge priority={content.priority} />
            <span>{content.category.name}</span>
            <span>Updated {formatDate(content.updatedAt)}</span>
          </div>
        </div>
        <div className="button-group">
          <button type="button" className="secondary" onClick={toggleBookmark}>
            {bookmarked ? "Remove Bookmark" : "Bookmark"}
          </button>
        </div>
      </header>
      {bookmarkMessage && <p className="inline-success">{bookmarkMessage}</p>}

      <AcknowledgementPanel content={content} onAcknowledged={() => setRefresh((value) => value + 1)} />

      <section className="markdown-body" dangerouslySetInnerHTML={{ __html: html }} />

      <section>
        <h2>Attachments</h2>
        {content.attachments.length === 0 ? (
          <EmptyState title="No attachments" />
        ) : (
          <div className="table-list">
            {content.attachments.map((attachment) => (
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

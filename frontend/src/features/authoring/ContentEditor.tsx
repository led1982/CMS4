import { FormEvent, useEffect, useMemo, useState } from "react";
import { marked } from "marked";
import { useNavigate, useParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { Category, ContentCreateRequest, ContentDetail, ContentType, Priority, useApi } from "../../services/apiClient";
import { getCategories, getContent } from "../portal/portalApi";
import { attachFile, createContent, submitForReview, updateContent } from "./contentWorkflowApi";
import { ContentWorkflowStates } from "./ContentWorkflowStates";

const defaultDraft: ContentCreateRequest = {
  contentType: "NOTICE",
  title: "",
  summary: "",
  body: "",
  categoryId: "cat-hr",
  audienceId: "aud-all",
  ownerUserId: "author",
  tags: ["Policy"],
  priority: "NORMAL",
  requiresAcknowledgement: false
};

export function ContentEditor() {
  const { contentId } = useParams();
  const navigate = useNavigate();
  const [draft, setDraft] = useState<ContentCreateRequest>(defaultDraft);
  const [content, setContent] = useState<ContentDetail | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [saving, setSaving] = useState(false);
  const contentState = useApi(() => (contentId ? getContent(contentId) : Promise.resolve(null)), [contentId]);
  const categoryState = useApi(getCategories, []);

  useEffect(() => {
    if (contentState.data) {
      const loaded = contentState.data;
      setContent(loaded);
      setDraft({
        contentType: loaded.contentType,
        title: loaded.title,
        summary: loaded.summary,
        body: loaded.body,
        categoryId: loaded.category.id,
        audienceId: loaded.audience.id,
        ownerUserId: loaded.owner.id,
        tags: loaded.tags,
        priority: loaded.priority,
        requiresAcknowledgement: loaded.requiresAcknowledgement,
        effectiveFrom: loaded.effectiveFrom,
        expiresAt: loaded.expiresAt
      });
    }
  }, [contentState.data]);

  const toc = useMemo(() => {
    return (draft.body ?? "")
      .split("\n")
      .filter((line) => line.startsWith("#"))
      .map((line) => line.replace(/^#+\s*/, ""));
  }, [draft.body]);

  if (contentState.loading || categoryState.loading) return <LoadingPanel label="Loading editor" />;
  if (contentState.error) return <ErrorState error={contentState.error} />;
  if (categoryState.error) return <ErrorState error={categoryState.error} />;

  async function save(event?: FormEvent) {
    event?.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const saved = content
        ? await updateContent(content.id, { ...draft, versionToken: content.versionToken, changeSummary: "Draft save" })
        : await createContent(draft);
      setContent(saved);
      setMessage("Draft saved");
      if (!content) {
        navigate(`/author/${saved.id}`, { replace: true });
      }
    } catch (reason) {
      setError(reason as Error);
    } finally {
      setSaving(false);
    }
  }

  async function submit() {
    if (!content) {
      await save();
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const submitted = await submitForReview(content.id, content.versionToken, "Submitted from editor");
      setContent(submitted);
      setMessage("Submitted for review");
    } catch (reason) {
      setError(reason as Error);
    } finally {
      setSaving(false);
    }
  }

  async function addAttachment() {
    if (!content) {
      setMessage("Save the draft before adding attachments");
      return;
    }
    const updated = await attachFile(content.id, { fileName: "policy-reference.pdf", mimeType: "application/pdf", sizeBytes: 256000 });
    setContent({ ...content, attachments: [...content.attachments, updated] });
  }

  const categories = categoryState.data ?? [];

  return (
    <div className="editor-layout">
      <form className="editor-main" onSubmit={save}>
        <div className="section-heading">
          <h1>{content ? "Edit Content" : "New Content"}</h1>
          <div className="button-group">
            <button type="submit" disabled={saving}>
              Save Draft
            </button>
            <button type="button" disabled={saving || content?.status === "IN_REVIEW"} onClick={submit}>
              Submit
            </button>
          </div>
        </div>
        {message && <p className="inline-success">{message}</p>}
        {error && <ErrorState error={error} />}
        <label>
          Title
          <input value={draft.title} onChange={(event) => setDraft({ ...draft, title: event.target.value })} required />
        </label>
        <label>
          Summary
          <textarea value={draft.summary} onChange={(event) => setDraft({ ...draft, summary: event.target.value })} rows={2} />
        </label>
        <div className="editor-split">
          <label>
            Markdown
            <textarea value={draft.body} onChange={(event) => setDraft({ ...draft, body: event.target.value })} rows={18} />
          </label>
          <section className="preview-pane">
            <h2>Preview</h2>
            <div className="markdown-body" dangerouslySetInnerHTML={{ __html: marked.parse(draft.body ?? "") as string }} />
          </section>
        </div>
      </form>

      <aside className="metadata-panel">
        {content ? <ContentWorkflowStates content={content} /> : <EmptyState title="Draft metadata" />}
        <label>
          Content type
          <select value={draft.contentType} onChange={(event) => setDraft({ ...draft, contentType: event.target.value as ContentType })}>
            <option value="NOTICE">Notice</option>
            <option value="KNOWLEDGE_ARTICLE">Knowledge article</option>
            <option value="DOCUMENT_RECORD">Document record</option>
          </select>
        </label>
        <label>
          Category
          <select value={draft.categoryId} onChange={(event) => setDraft({ ...draft, categoryId: event.target.value })}>
            {flattenCategories(categories).map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Priority
          <select value={draft.priority} onChange={(event) => setDraft({ ...draft, priority: event.target.value as Priority })}>
            <option value="NORMAL">Normal</option>
            <option value="PINNED">Pinned</option>
            <option value="URGENT">Urgent</option>
          </select>
        </label>
        <label className="checkbox-row">
          <input
            type="checkbox"
            checked={Boolean(draft.requiresAcknowledgement)}
            onChange={(event) => setDraft({ ...draft, requiresAcknowledgement: event.target.checked })}
          />
          Requires acknowledgement
        </label>
        <section>
          <h2>Table of Contents</h2>
          {toc.length === 0 ? <EmptyState title="No headings" /> : <ol>{toc.map((heading) => <li key={heading}>{heading}</li>)}</ol>}
        </section>
        <section>
          <h2>Attachments</h2>
          <button type="button" onClick={addAttachment}>
            Add PDF
          </button>
          {content?.attachments.map((attachment) => (
            <p key={attachment.id}>
              {attachment.fileName} · {attachment.validationStatus}
            </p>
          ))}
        </section>
      </aside>
    </div>
  );
}

function flattenCategories(categories: Category[]): Category[] {
  return categories.flatMap((category) => [category, ...flattenCategories(category.children ?? [])]);
}

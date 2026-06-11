import { FormEvent, useState } from "react";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { useApi } from "../../services/apiClient";
import { getCategories } from "../portal/portalApi";
import { createCategory, createTag, listTags } from "./adminApi";

export function TaxonomyAdmin() {
  const [categoryName, setCategoryName] = useState("");
  const [tagName, setTagName] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const categories = useApi(getCategories, [message]);
  const tags = useApi(listTags, [message]);

  async function submitCategory(event: FormEvent) {
    event.preventDefault();
    await createCategory({ name: categoryName, ownerUserId: "admin", defaultAudienceId: "aud-all", sortOrder: 50 });
    setCategoryName("");
    setMessage("Category created");
  }

  async function submitTag(event: FormEvent) {
    event.preventDefault();
    await createTag({ name: tagName });
    setTagName("");
    setMessage("Tag created");
  }

  return (
    <div className="page-layout with-sidebar">
      <aside className="side-panel admin-nav">
        <a href="/admin/taxonomy">Taxonomy</a>
        <a href="/admin/access">Access</a>
        <a href="/admin/audit">Audit</a>
      </aside>
      <section>
        <h1>Taxonomy Administration</h1>
        {message && <p className="inline-success">{message}</p>}
        {(categories.loading || tags.loading) && <LoadingPanel />}
        {(categories.error || tags.error) && <ErrorState error={(categories.error ?? tags.error)!} />}
        <div className="admin-grid">
          <form onSubmit={submitCategory}>
            <h2>New Category</h2>
            <label>
              Name
              <input value={categoryName} onChange={(event) => setCategoryName(event.target.value)} required />
            </label>
            <button type="submit">Create Category</button>
          </form>
          <form onSubmit={submitTag}>
            <h2>New Tag</h2>
            <label>
              Name
              <input value={tagName} onChange={(event) => setTagName(event.target.value)} required />
            </label>
            <button type="submit">Create Tag</button>
          </form>
        </div>
        <h2>Categories</h2>
        {!categories.data?.length ? (
          <EmptyState title="No categories" />
        ) : (
          <div className="table-list">
            {categories.data.map((category) => (
              <div className="table-row" key={category.id}>
                <span>{category.name}</span>
                <span>{category.slug}</span>
                <span>{category.active ? "Active" : "Inactive"}</span>
              </div>
            ))}
          </div>
        )}
        <h2>Tags</h2>
        <div className="tag-cloud">{tags.data?.map((tag) => <span key={tag.id}>{tag.name}</span>)}</div>
      </section>
    </div>
  );
}

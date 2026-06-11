import { FormEvent, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { PriorityBadge, StatusBadge } from "../../components/StatusBadge";
import { ContentType, formatDate, useApi } from "../../services/apiClient";
import { searchContent } from "./portalApi";

export function SearchResults() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [query, setQuery] = useState(searchParams.get("q") ?? "");
  const [type, setType] = useState<ContentType | "">((searchParams.get("contentType") as ContentType | null) ?? "");
  const categoryId = searchParams.get("categoryId") ?? undefined;

  const deps = useMemo(() => [searchParams.toString()], [searchParams]);
  const { data, error, loading } = useApi(
    () =>
      searchContent({
        q: searchParams.get("q") ?? undefined,
        contentType: (searchParams.get("contentType") as ContentType | null) ?? undefined,
        categoryId,
        acknowledgementRequired: searchParams.get("acknowledgementRequired") === "true" ? true : undefined,
        size: 50
      }),
    deps
  );

  function submit(event: FormEvent) {
    event.preventDefault();
    const next = new URLSearchParams(searchParams);
    query ? next.set("q", query) : next.delete("q");
    type ? next.set("contentType", type) : next.delete("contentType");
    setSearchParams(next);
  }

  return (
    <div className="page-layout with-sidebar">
      <aside className="side-panel">
        <h1>Search</h1>
        <form className="filter-form" onSubmit={submit}>
          <label>
            Keyword
            <input value={query} onChange={(event) => setQuery(event.target.value)} />
          </label>
          <label>
            Type
            <select value={type} onChange={(event) => setType(event.target.value as ContentType | "")}>
              <option value="">All</option>
              <option value="KNOWLEDGE_ARTICLE">Knowledge article</option>
              <option value="DOCUMENT_RECORD">Document record</option>
              <option value="NOTICE">Notice</option>
            </select>
          </label>
          <button type="submit">Apply</button>
        </form>
      </aside>

      <section>
        <h2>{data ? `${data.page.totalItems} results` : "Results"}</h2>
        {loading && <LoadingPanel label="Loading search results" />}
        {error && <ErrorState error={error} />}
        {data && data.items.length === 0 && <EmptyState title="No matching content" action={<Link to="/categories">Browse categories</Link>} />}
        {data && data.items.length > 0 && (
          <div className="list-stack">
            {data.items.map((item) => (
              <article className="content-row" key={item.id}>
                <div>
                  <Link to={`/content/${item.id}`}>{item.title}</Link>
                  <p>{item.summary}</p>
                  <small>
                    {item.category.name} · {item.contentType.replace("_", " ")} · {formatDate(item.updatedAt)}
                  </small>
                </div>
                <div className="row-meta">
                  <StatusBadge status={item.status} />
                  <PriorityBadge priority={item.priority} />
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import { apiRequest, getPersona, setPersona, UserProfile } from "../services/apiClient";
import { PortalHome } from "../features/portal/PortalHome";
import { SearchResults } from "../features/portal/SearchResults";
import { CategoryBrowse } from "../features/portal/CategoryBrowse";
import { ContentDetail } from "../features/portal/ContentDetail";
import { MyAcknowledgements } from "../features/portal/MyAcknowledgements";
import { AuthorWorkspace } from "../features/authoring/AuthorWorkspace";
import { ContentEditor } from "../features/authoring/ContentEditor";
import { ReviewQueue } from "../features/review/ReviewQueue";
import { TaxonomyAdmin } from "../features/admin/TaxonomyAdmin";
import { AccessAdmin } from "../features/admin/AccessAdmin";
import { AuditLog } from "../features/admin/AuditLog";
import { AcknowledgementDashboard } from "../features/editor/AcknowledgementDashboard";

export function AppShell() {
  const [persona, setPersonaState] = useState(getPersona());
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [query, setQuery] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    apiRequest<UserProfile>("/api/v1/me").then(setProfile).catch(() => setProfile(null));
    const listener = () => {
      setPersonaState(getPersona());
      apiRequest<UserProfile>("/api/v1/me").then(setProfile).catch(() => setProfile(null));
    };
    window.addEventListener("cms-persona-changed", listener);
    return () => window.removeEventListener("cms-persona-changed", listener);
  }, [persona]);

  const roles = profile?.roles ?? ["EMPLOYEE"];
  const canAuthor = useMemo(() => roles.some((role) => ["AUTHOR", "NOTICE_MANAGER", "EDITOR", "ADMINISTRATOR"].includes(role)), [roles]);
  const canReview = useMemo(() => roles.some((role) => ["REVIEWER", "EDITOR", "ADMINISTRATOR"].includes(role)), [roles]);
  const canEdit = useMemo(() => roles.some((role) => ["NOTICE_MANAGER", "EDITOR", "ADMINISTRATOR"].includes(role)), [roles]);
  const canAdmin = useMemo(() => roles.includes("ADMINISTRATOR"), [roles]);

  function onSearch(event: FormEvent) {
    event.preventDefault();
    navigate(`/search?q=${encodeURIComponent(query)}`);
  }

  return (
    <div className="app-shell">
      <header className="global-header">
        <Link className="brand" to="/">
          Company CMS
        </Link>
        <form className="header-search" onSubmit={onSearch} role="search">
          <label className="sr-only" htmlFor="global-search">
            Search official content
          </label>
          <input id="global-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search official content" />
          <button type="submit" title="Search">
            Search
          </button>
        </form>
        <div className="persona-control">
          <span>{profile?.displayName ?? "Loading user"}</span>
          <select
            aria-label="Development persona"
            value={persona}
            onChange={(event) => {
              setPersona(event.target.value);
              setPersonaState(event.target.value);
            }}
          >
            <option value="employee">Employee</option>
            <option value="employee.hr">HR Employee</option>
            <option value="employee.eng">Engineering Employee</option>
            <option value="author">Author</option>
            <option value="author.hr">HR Author</option>
            <option value="reviewer">Reviewer</option>
            <option value="reviewer.hr">HR Reviewer</option>
            <option value="publisher.hr">HR Publisher</option>
            <option value="notice.manager">Notice Manager</option>
            <option value="auditor.cms">Auditor</option>
            <option value="editor">Editor</option>
            <option value="admin">Admin</option>
            <option value="admin.cms">CMS Admin</option>
          </select>
        </div>
      </header>

      <nav className="primary-nav" aria-label="Primary">
        <NavLink to="/">Home</NavLink>
        <NavLink to="/categories">Categories</NavLink>
        <NavLink to="/search">Search</NavLink>
        <NavLink to="/acknowledgements">My Acknowledgements</NavLink>
        {canAuthor && <NavLink to="/author">Author</NavLink>}
        {canReview && <NavLink to="/review">Review</NavLink>}
        {canEdit && <NavLink to="/editor/acknowledgements">Editorial</NavLink>}
        {canAdmin && <NavLink to="/admin/taxonomy">Admin</NavLink>}
      </nav>

      <main className="main-surface">
        <Routes>
          <Route path="/" element={<PortalHome />} />
          <Route path="/search" element={<SearchResults />} />
          <Route path="/categories" element={<CategoryBrowse />} />
          <Route path="/content/:contentId" element={<ContentDetail />} />
          <Route path="/acknowledgements" element={<MyAcknowledgements />} />
          <Route path="/author" element={<AuthorWorkspace />} />
          <Route path="/author/new" element={<ContentEditor />} />
          <Route path="/author/:contentId" element={<ContentEditor />} />
          <Route path="/review" element={<ReviewQueue />} />
          <Route path="/editor/acknowledgements" element={<AcknowledgementDashboard />} />
          <Route path="/admin/taxonomy" element={<TaxonomyAdmin />} />
          <Route path="/admin/access" element={<AccessAdmin />} />
          <Route path="/admin/audit" element={<AuditLog />} />
        </Routes>
      </main>
    </div>
  );
}

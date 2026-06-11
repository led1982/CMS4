import { FormEvent, useState } from "react";
import { EmptyState, ErrorState, LoadingPanel } from "../../components/StatePanel";
import { RoleCode, useApi } from "../../services/apiClient";
import { createRoleAssignment, listAudiences, listRoleAssignments } from "./adminApi";

export function AccessAdmin() {
  const [userId, setUserId] = useState("author");
  const [role, setRole] = useState<RoleCode>("AUTHOR");
  const [message, setMessage] = useState<string | null>(null);
  const audiences = useApi(listAudiences, []);
  const assignments = useApi(() => listRoleAssignments(), [message]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    await createRoleAssignment({ userId, role, scopeType: "GLOBAL" });
    setMessage("Role assignment created");
  }

  return (
    <div className="page-layout with-sidebar">
      <aside className="side-panel admin-nav">
        <a href="/admin/taxonomy">Taxonomy</a>
        <a href="/admin/access">Access</a>
        <a href="/admin/audit">Audit</a>
      </aside>
      <section>
        <h1>Audience & Role Administration</h1>
        {message && <p className="inline-success">{message}</p>}
        {(audiences.loading || assignments.loading) && <LoadingPanel />}
        {(audiences.error || assignments.error) && <ErrorState error={(audiences.error ?? assignments.error)!} />}
        <form className="admin-grid" onSubmit={submit}>
          <label>
            User ID
            <input value={userId} onChange={(event) => setUserId(event.target.value)} />
          </label>
          <label>
            Role
            <select value={role} onChange={(event) => setRole(event.target.value as RoleCode)}>
              <option value="AUTHOR">Author</option>
              <option value="REVIEWER">Reviewer</option>
              <option value="EDITOR">Editor</option>
              <option value="ADMINISTRATOR">Administrator</option>
            </select>
          </label>
          <button type="submit">Assign Role</button>
        </form>
        <h2>Audiences</h2>
        {!audiences.data?.length ? <EmptyState title="No audiences" /> : <div className="tag-cloud">{audiences.data.map((audience) => <span key={audience.id}>{audience.name}</span>)}</div>}
        <h2>Role Assignments</h2>
        <div className="table-list">
          {assignments.data?.map((assignment) => (
            <div className="table-row" key={assignment.id}>
              <span>{assignment.user.displayName}</span>
              <span>{assignment.role}</span>
              <span>{assignment.scopeType}</span>
              <span>{assignment.scopeId ?? "Global"}</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createScopeRequest, DomainDependencyInput, ScopeRequestCreate } from "../services/scopeRequestClient";

const defaultCriteria = [
  "selected and auto-added effective domains have spec.md, plan.md, and tasks.md drafts",
  "generation_order stage order is not violated",
  "each domain requires dependency is satisfied in the same or previous stage"
].join("\n");

interface ImportFormState {
  requestCode: string;
  slug: string;
  requestType: ScopeRequestCreate["requestType"];
  title: string;
  goal: string;
  selectedDomains: string;
  autoAddedDomains: string;
  declaredEffectiveDomains: string;
  dependencies: string;
  acceptanceCriteria: string;
  sourceText: string;
}

export function ImportRequestPage() {
  const navigate = useNavigate();
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState<ImportFormState>({
    requestCode: "CMS4-4",
    slug: "cms4",
    requestType: "new",
    title: "cms",
    goal: "cms",
    selectedDomains: "",
    autoAddedDomains: "",
    declaredEffectiveDomains: "",
    dependencies: "",
    acceptanceCriteria: defaultCriteria,
    sourceText: "SPEC-REQUEST.md"
  });

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const created = await createScopeRequest(toPayload(form));
      navigate(`/scope-requests/${created.id}`);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Could not import the request.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="scope-layout" onSubmit={onSubmit}>
      <section className="section-full">
        <div className="section-heading">
          <h1>Import Request</h1>
          <button type="submit" disabled={saving}>
            {saving ? "Importing" : "Import"}
          </button>
        </div>
        {error && (
          <div className="state-panel state-panel-error" role="alert">
            {error}
          </div>
        )}
        <div className="form-grid">
          <Field label="Request code" value={form.requestCode} onChange={(value) => setForm({ ...form, requestCode: value })} />
          <Field label="Slug" value={form.slug} onChange={(value) => setForm({ ...form, slug: value })} />
          <label className="field-stack">
            <span>Request type</span>
            <select value={form.requestType} onChange={(event) => setForm({ ...form, requestType: event.target.value as ScopeRequestCreate["requestType"] })}>
              <option value="new">new</option>
              <option value="change">change</option>
              <option value="fix">fix</option>
            </select>
          </label>
          <Field label="Title" value={form.title} onChange={(value) => setForm({ ...form, title: value })} />
          <label className="field-stack form-wide">
            <span>Goal</span>
            <textarea rows={3} value={form.goal} onChange={(event) => setForm({ ...form, goal: event.target.value })} />
          </label>
          <TextArea label="Selected domains" value={form.selectedDomains} onChange={(value) => setForm({ ...form, selectedDomains: value })} />
          <TextArea label="Auto-added domains" value={form.autoAddedDomains} onChange={(value) => setForm({ ...form, autoAddedDomains: value })} />
          <TextArea label="Declared effective domains" value={form.declaredEffectiveDomains} onChange={(value) => setForm({ ...form, declaredEffectiveDomains: value })} />
          <TextArea label="Dependencies" value={form.dependencies} onChange={(value) => setForm({ ...form, dependencies: value })} placeholder="cms-admin: cms-core" />
          <TextArea label="Acceptance criteria" value={form.acceptanceCriteria} onChange={(value) => setForm({ ...form, acceptanceCriteria: value })} wide />
          <TextArea label="Source text" value={form.sourceText} onChange={(value) => setForm({ ...form, sourceText: value })} wide />
        </div>
      </section>
    </form>
  );
}

function Field({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <label className="field-stack">
      <span>{label}</span>
      <input value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function TextArea({ label, value, onChange, placeholder, wide = false }: { label: string; value: string; onChange: (value: string) => void; placeholder?: string; wide?: boolean }) {
  return (
    <label className={`field-stack ${wide ? "form-wide" : ""}`}>
      <span>{label}</span>
      <textarea rows={wide ? 5 : 3} value={value} placeholder={placeholder} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function toPayload(form: ImportFormState): ScopeRequestCreate {
  return {
    requestCode: form.requestCode,
    slug: form.slug,
    requestType: form.requestType,
    title: form.title,
    goal: form.goal,
    selectedDomains: parseLines(form.selectedDomains),
    autoAddedDomains: parseLines(form.autoAddedDomains),
    declaredEffectiveDomains: parseLines(form.declaredEffectiveDomains),
    dependencies: parseDependencies(form.dependencies),
    acceptanceCriteria: parseLines(form.acceptanceCriteria),
    sourceText: form.sourceText
  };
}

function parseLines(value: string) {
  return value
    .split(/\r?\n|,/)
    .map((line) => line.trim())
    .filter(Boolean);
}

function parseDependencies(value: string): DomainDependencyInput[] {
  return value
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) => {
      const [domainKey, requires = ""] = line.split(":");
      return {
        domainKey: domainKey.trim(),
        requires: parseLines(requires)
      };
    });
}

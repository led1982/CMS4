const baseUrl = process.env.CMS_SCOPE_API_URL ?? "http://localhost:3000";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    }
  });
  if (!response.ok) {
    throw new Error(`${init?.method ?? "GET"} ${path} failed with ${response.status}: ${await response.text()}`);
  }
  return response.json() as Promise<T>;
}

interface ScopeRequestResponse {
  id: string;
}

interface DomainResolutionResult {
  domains: Array<{ domainKey: string; source: string; generationStage: number }>;
}

interface ArtifactGenerationResult {
  artifactSets: Array<{ domainKey: string; status: string; artifacts: Array<{ artifactType: string }> }>;
}

interface ValidationReport {
  status: string;
  errorCount: number;
}

const created = await request<ScopeRequestResponse>("/scope-requests", {
  method: "POST",
  body: JSON.stringify({
    requestCode: "CMS4-4",
    slug: "cms4",
    requestType: "new",
    title: "cms",
    goal: "cms",
    selectedDomains: [],
    autoAddedDomains: [],
    declaredEffectiveDomains: [],
    dependencies: [],
    acceptanceCriteria: [
      "selected and auto-added effective domains have spec.md, plan.md, and tasks.md drafts",
      "generation_order stage order is not violated",
      "each domain requires dependency is satisfied in the same or previous stage"
    ],
    sourceText: "SPEC-REQUEST.md"
  })
});

const resolution = await request<DomainResolutionResult>(`/scope-requests/${created.id}/resolve-domains`, { method: "POST" });
const domain = resolution.domains[0];
if (resolution.domains.length !== 1 || domain.domainKey !== "cms-core" || domain.source !== "default" || domain.generationStage !== 1) {
  throw new Error("CMS4-4 did not resolve to default cms-core stage 1.");
}

const generation = await request<ArtifactGenerationResult>(`/scope-requests/${created.id}/generate-artifacts`, {
  method: "POST",
  body: JSON.stringify({ forceRegenerate: false })
});
if (generation.artifactSets.length !== 1 || generation.artifactSets[0].status !== "complete" || generation.artifactSets[0].artifacts.length !== 3) {
  throw new Error("CMS4-4 did not generate a complete artifact set.");
}

const validation = await request<ValidationReport>(`/scope-requests/${created.id}/validate`, { method: "POST" });
if (validation.status !== "passed" || validation.errorCount !== 0) {
  throw new Error("CMS4-4 validation did not pass.");
}

console.info(`CMS4-4 smoke validation passed for request ${created.id}`);

import { describe, expect, it } from "vitest";
import { buildApp } from "../../src/server.js";

describe("CMS4-4 artifact generation", () => {
  it("resolves cms-core and generates spec, plan, and tasks drafts", async () => {
    const app = buildApp();
    const created = await app.inject({ method: "POST", url: "/scope-requests", payload: cms4Payload() });
    const requestId = created.json().id as string;

    await app.inject({ method: "POST", url: `/scope-requests/${requestId}/resolve-domains` });
    const generated = await app.inject({ method: "POST", url: `/scope-requests/${requestId}/generate-artifacts`, payload: { forceRegenerate: false } });
    const body = generated.json();

    expect(generated.statusCode).toBe(202);
    expect(body.artifactSets).toHaveLength(1);
    expect(body.artifactSets[0].domainKey).toBe("cms-core");
    expect(body.artifactSets[0].status).toBe("complete");
    expect(body.artifactSets[0].artifacts.map((artifact: { artifactType: string }) => artifact.artifactType).sort()).toEqual(["plan", "spec", "tasks"]);

    const spec = await app.inject({ method: "GET", url: `/scope-requests/${requestId}/artifact-sets/cms-core/artifacts/spec` });
    expect(spec.json().content).toContain("Feature Specification: cms");
    expect(spec.json().content).toContain("Empty CMS input defaulted to cms-core.");
    await app.close();
  });
});

function cms4Payload() {
  return {
    requestCode: "CMS4-4",
    slug: "cms4",
    requestType: "new",
    title: "cms",
    goal: "cms",
    selectedDomains: [],
    autoAddedDomains: [],
    declaredEffectiveDomains: [],
    dependencies: [],
    acceptanceCriteria: ["generate draft artifacts"],
    sourceText: "SPEC-REQUEST.md"
  };
}

import { describe, expect, it } from "vitest";
import { buildApp } from "../../src/server.js";

describe("CMS4-4 idempotency", () => {
  it("does not duplicate artifact sets when generation is rerun unchanged", async () => {
    const app = buildApp();
    const created = await app.inject({ method: "POST", url: "/scope-requests", payload: cms4Payload() });
    const requestId = created.json().id as string;

    await app.inject({ method: "POST", url: `/scope-requests/${requestId}/resolve-domains` });
    const first = await app.inject({ method: "POST", url: `/scope-requests/${requestId}/generate-artifacts`, payload: { forceRegenerate: false } });
    const second = await app.inject({ method: "POST", url: `/scope-requests/${requestId}/generate-artifacts`, payload: { forceRegenerate: false } });
    const listed = await app.inject({ method: "GET", url: `/scope-requests/${requestId}/artifact-sets` });

    expect(first.json().artifactSets[0].contentFingerprint).toBe(second.json().artifactSets[0].contentFingerprint);
    expect(listed.json().items).toHaveLength(1);
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

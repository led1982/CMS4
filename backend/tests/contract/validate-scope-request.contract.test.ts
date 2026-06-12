import { describe, expect, it } from "vitest";
import { buildApp } from "../../src/server.js";

describe("POST /scope-requests/{requestId}/validate", () => {
  it("validates a generated CMS4-4 scope request", async () => {
    const app = buildApp();
    const created = await app.inject({ method: "POST", url: "/scope-requests", payload: cms4Payload() });
    const requestId = created.json().id as string;

    await app.inject({ method: "POST", url: `/scope-requests/${requestId}/resolve-domains` });
    await app.inject({ method: "POST", url: `/scope-requests/${requestId}/generate-artifacts`, payload: { forceRegenerate: false } });
    const response = await app.inject({ method: "POST", url: `/scope-requests/${requestId}/validate` });

    expect(response.statusCode).toBe(200);
    expect(response.json()).toMatchObject({
      status: "passed",
      errorCount: 0,
      warningCount: 0,
      findings: []
    });
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

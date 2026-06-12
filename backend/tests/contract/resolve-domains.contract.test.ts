import { describe, expect, it } from "vitest";
import { buildApp } from "../../src/server.js";

describe("POST /scope-requests/{requestId}/resolve-domains", () => {
  it("returns default cms-core resolution for empty CMS input", async () => {
    const app = buildApp();
    const created = await app.inject({ method: "POST", url: "/scope-requests", payload: cms4Payload() });
    const requestId = created.json().id as string;

    const response = await app.inject({ method: "POST", url: `/scope-requests/${requestId}/resolve-domains` });

    expect(response.statusCode).toBe(200);
    expect(response.json()).toMatchObject({
      requestId,
      domains: [{ domainKey: "cms-core", source: "default", generationStage: 1 }],
      assumptions: ["Empty CMS input defaulted to cms-core."]
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

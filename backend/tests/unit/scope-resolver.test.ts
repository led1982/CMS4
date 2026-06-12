import { describe, expect, it } from "vitest";
import { ScopeRequest } from "../../src/models/scope-request.js";
import { ScopeResolver } from "../../src/services/scope-resolver.js";

describe("ScopeResolver", () => {
  it("defaults empty CMS domain input to cms-core", () => {
    const request = makeRequest();
    const result = new ScopeResolver().resolve(request);

    expect(result.domains).toHaveLength(1);
    expect(result.domains[0]).toMatchObject({
      domainKey: "cms-core",
      source: "default",
      requires: [],
      generationStage: 1
    });
    expect(result.assumptions).toContain("Empty CMS input defaulted to cms-core.");
  });
});

function makeRequest(): ScopeRequest {
  const now = new Date().toISOString();
  return {
    id: "request-1",
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
    sourceText: "SPEC-REQUEST.md",
    status: "draft",
    createdAt: now,
    updatedAt: now
  };
}

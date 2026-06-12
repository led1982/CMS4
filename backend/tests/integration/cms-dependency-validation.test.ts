import { describe, expect, it } from "vitest";
import { buildApp } from "../../src/server.js";

describe("CMS dependency validation", () => {
  it("fails when a dependency is assigned to a later generation stage", async () => {
    const app = buildApp();
    const created = await app.inject({
      method: "POST",
      url: "/scope-requests",
      payload: {
        requestCode: "CMS-DEP",
        slug: "cms-dep",
        requestType: "new",
        title: "cms",
        goal: "cms",
        selectedDomains: ["cms-core", "cms-admin"],
        autoAddedDomains: [],
        declaredEffectiveDomains: [],
        dependencies: [{ domainKey: "cms-admin", requires: ["cms-core"] }],
        acceptanceCriteria: ["validate dependency order"],
        sourceText: "synthetic"
      }
    });
    const requestId = created.json().id as string;

    await app.inject({ method: "POST", url: `/scope-requests/${requestId}/resolve-domains` });
    await app.inject({
      method: "PUT",
      url: `/scope-requests/${requestId}/generation-order`,
      payload: {
        stages: [
          { stageNumber: 1, domainKeys: ["cms-admin"] },
          { stageNumber: 2, domainKeys: ["cms-core"] }
        ]
      }
    });
    await app.inject({ method: "POST", url: `/scope-requests/${requestId}/generate-artifacts`, payload: { forceRegenerate: false } });
    const response = await app.inject({ method: "POST", url: `/scope-requests/${requestId}/validate` });

    expect(response.statusCode).toBe(200);
    expect(response.json().status).toBe("failed");
    expect(response.json().findings).toEqual(expect.arrayContaining([expect.objectContaining({ findingType: "later_stage_dependency", domainKey: "cms-admin", relatedDomainKey: "cms-core" })]));
    await app.close();
  });
});

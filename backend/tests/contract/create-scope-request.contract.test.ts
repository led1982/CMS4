import { describe, expect, it } from "vitest";
import { buildApp } from "../../src/server.js";

describe("POST /scope-requests", () => {
  it("creates a CMS scope request", async () => {
    const app = buildApp();
    const response = await app.inject({
      method: "POST",
      url: "/scope-requests",
      payload: cms4Payload()
    });

    expect(response.statusCode).toBe(201);
    expect(response.json()).toMatchObject({
      requestCode: "CMS4-4",
      slug: "cms4",
      status: "draft"
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

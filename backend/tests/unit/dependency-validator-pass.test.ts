import { describe, expect, it } from "vitest";
import { ArtifactSet } from "../../src/models/artifact-set.js";
import { Domain } from "../../src/models/domain.js";
import { DependencyValidator } from "../../src/services/dependency-validator.js";

describe("DependencyValidator pass cases", () => {
  it("passes no-dependency cms-core in stage 1", () => {
    const domains = [domain("cms-core", 1, [])];
    const report = new DependencyValidator().validate(domains, [artifactSet("cms-core")]);

    expect(report.status).toBe("passed");
    expect(report.errorCount).toBe(0);
  });

  it("passes same-stage and previous-stage dependencies", () => {
    const domains = [domain("cms-core", 1, []), domain("cms-admin", 1, ["cms-core"]), domain("cms-portal", 2, ["cms-core"])];
    const report = new DependencyValidator().validate(domains, [artifactSet("cms-core"), artifactSet("cms-admin"), artifactSet("cms-portal")]);

    expect(report.status).toBe("passed");
    expect(report.findings).toHaveLength(0);
  });
});

function domain(domainKey: string, generationStage: number, requires: string[]): Domain {
  return {
    id: domainKey,
    scopeRequestId: "request-1",
    domainKey,
    displayName: domainKey,
    source: "selected",
    requires,
    generationStage,
    description: "",
    status: "planned"
  };
}

function artifactSet(domainKey: string): ArtifactSet {
  const generatedAt = new Date().toISOString();
  return {
    id: `set-${domainKey}`,
    scopeRequestId: "request-1",
    domainKey,
    artifactStatuses: { spec: "generated", plan: "generated", tasks: "generated" },
    contentFingerprint: domainKey,
    status: "complete",
    generatedAt,
    artifacts: ["spec", "plan", "tasks"].map((artifactType) => ({
      artifactSetId: `set-${domainKey}`,
      artifactType: artifactType as "spec" | "plan" | "tasks",
      path: `${domainKey}/${artifactType}.md`,
      status: "generated",
      title: `${artifactType}.md`,
      generatedFromTemplate: "test",
      contentFingerprint: `${domainKey}-${artifactType}`,
      content: "# Test"
    }))
  };
}

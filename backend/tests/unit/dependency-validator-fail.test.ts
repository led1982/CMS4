import { describe, expect, it } from "vitest";
import { ArtifactSet } from "../../src/models/artifact-set.js";
import { Domain } from "../../src/models/domain.js";
import { DependencyValidator } from "../../src/services/dependency-validator.js";

describe("DependencyValidator failure cases", () => {
  it("reports missing, self, circular, and later-stage dependency failures", () => {
    const domains = [
      domain("cms-core", 2, ["cms-admin"]),
      domain("cms-admin", 1, ["cms-core", "missing-domain"]),
      domain("cms-self", 1, ["cms-self"])
    ];
    const report = new DependencyValidator().validate(domains, domains.map((item) => artifactSet(item.domainKey)));

    expect(report.status).toBe("failed");
    expect(report.findings.map((finding) => finding.findingType)).toEqual(
      expect.arrayContaining(["later_stage_dependency", "missing_dependency", "self_dependency", "cycle"])
    );
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

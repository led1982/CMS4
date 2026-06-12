import { createHash, randomUUID } from "node:crypto";
import { Artifact, ArtifactSet, ArtifactType, REQUIRED_ARTIFACT_TYPES } from "../models/artifact-set.js";
import { Domain } from "../models/domain.js";
import { ScopeRequest } from "../models/scope-request.js";
import { computeArtifactSetStatus } from "../repositories/artifact-repository.js";

const TEMPLATE_VERSION = "cms-scope-template-v1";

export class ArtifactGenerator {
  generate(request: ScopeRequest, domains: Domain[], assumptions: string[]): ArtifactSet[] {
    return domains.map((domain) => this.generateForDomain(request, domain, assumptions));
  }

  private generateForDomain(request: ScopeRequest, domain: Domain, assumptions: string[]): ArtifactSet {
    const artifactSetId = randomUUID();
    const fingerprintInput = JSON.stringify({
      template: TEMPLATE_VERSION,
      requestCode: request.requestCode,
      title: request.title,
      goal: request.goal,
      domainKey: domain.domainKey,
      requires: domain.requires,
      acceptanceCriteria: request.acceptanceCriteria,
      assumptions
    });
    const contentFingerprint = fingerprint(fingerprintInput);
    const artifacts = REQUIRED_ARTIFACT_TYPES.map((artifactType) =>
      this.renderArtifact({
        artifactSetId,
        artifactType,
        request,
        domain,
        assumptions,
        contentFingerprint
      })
    );

    return {
      id: artifactSetId,
      scopeRequestId: request.id,
      domainKey: domain.domainKey,
      artifactStatuses: Object.fromEntries(artifacts.map((artifact) => [artifact.artifactType, artifact.status])) as ArtifactSet["artifactStatuses"],
      contentFingerprint,
      status: computeArtifactSetStatus(artifacts),
      generatedAt: new Date().toISOString(),
      artifacts
    };
  }

  private renderArtifact(input: {
    artifactSetId: string;
    artifactType: ArtifactType;
    request: ScopeRequest;
    domain: Domain;
    assumptions: string[];
    contentFingerprint: string;
  }): Artifact {
    const fileName = `${input.artifactType}.md`;
    const content = renderMarkdown(input.artifactType, input.request, input.domain, input.assumptions);
    return {
      artifactSetId: input.artifactSetId,
      artifactType: input.artifactType,
      path: `.specify/specs/001-feature/${input.domain.domainKey}/${fileName}`,
      status: "generated",
      title: titleFor(input.artifactType),
      generatedFromTemplate: TEMPLATE_VERSION,
      contentFingerprint: fingerprint(`${input.contentFingerprint}:${input.artifactType}:${content}`),
      content
    };
  }
}

function renderMarkdown(artifactType: ArtifactType, request: ScopeRequest, domain: Domain, assumptions: string[]): string {
  const dependencies = domain.requires.length === 0 ? "No domain dependencies declared." : domain.requires.map((dependency) => `- ${dependency}`).join("\n");
  const acceptedAssumptions = assumptions.length === 0 ? "- No default assumptions were applied." : assumptions.map((assumption) => `- ${assumption}`).join("\n");
  const acceptanceCriteria = request.acceptanceCriteria.map((criterion) => `- ${criterion}`).join("\n");

  if (artifactType === "spec") {
    return `# Feature Specification: ${request.title}

**Request Code**: ${request.requestCode}
**Domain**: ${domain.displayName} (${domain.domainKey})
**Goal**: ${request.goal}

## Accepted Default Assumptions

${acceptedAssumptions}

## Dependencies

${dependencies}

## Acceptance Criteria

${acceptanceCriteria}
`;
  }

  if (artifactType === "plan") {
    return `# Implementation Plan: ${request.title}

## Domain

${domain.displayName} is assigned to generation_order stage ${domain.generationStage}.

## Goal

${request.goal}

## Dependency Plan

${dependencies}

## Assumptions

${acceptedAssumptions}

## Validation Targets

${acceptanceCriteria}
`;
  }

  return `# Tasks: ${request.title}

## Domain

${domain.displayName} (${domain.domainKey})

## Generation Order

- Stage ${domain.generationStage}: ${domain.domainKey}

## Dependency Checks

${dependencies}

## Draft Artifact Tasks

- [ ] Draft spec.md for ${domain.domainKey}
- [ ] Draft plan.md for ${domain.domainKey}
- [ ] Draft tasks.md for ${domain.domainKey}

## Acceptance Criteria

${acceptanceCriteria}

## Assumptions

${acceptedAssumptions}
`;
}

function titleFor(artifactType: ArtifactType): string {
  if (artifactType === "spec") {
    return "spec.md";
  }
  if (artifactType === "plan") {
    return "plan.md";
  }
  return "tasks.md";
}

function fingerprint(value: string): string {
  return createHash("sha256").update(value).digest("hex");
}

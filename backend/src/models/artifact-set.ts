export type ArtifactType = "spec" | "plan" | "tasks";
export type ArtifactStatus = "draft" | "generated" | "validated" | "incomplete" | "error";
export type ArtifactSetStatus = "pending" | "complete" | "incomplete" | "validated" | "error";

export const REQUIRED_ARTIFACT_TYPES: ArtifactType[] = ["spec", "plan", "tasks"];

export interface ArtifactSummary {
  artifactType: ArtifactType;
  path: string;
  status: ArtifactStatus;
}

export interface Artifact extends ArtifactSummary {
  artifactSetId: string;
  title: string;
  generatedFromTemplate: string;
  contentFingerprint: string;
  content: string;
}

export interface ArtifactSet {
  id: string;
  scopeRequestId: string;
  domainKey: string;
  artifactStatuses: Record<ArtifactType, ArtifactStatus>;
  contentFingerprint: string;
  status: ArtifactSetStatus;
  generatedAt: string;
  validatedAt?: string;
  artifacts: Artifact[];
}

export function summarizeArtifactSet(artifactSet: ArtifactSet) {
  return {
    domainKey: artifactSet.domainKey,
    status: artifactSet.status,
    contentFingerprint: artifactSet.contentFingerprint,
    generatedAt: artifactSet.generatedAt,
    validatedAt: artifactSet.validatedAt,
    artifacts: artifactSet.artifacts.map(({ artifactType, path, status }) => ({ artifactType, path, status }))
  };
}

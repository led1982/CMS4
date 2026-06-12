import { Artifact, ArtifactSet, ArtifactSetStatus, ArtifactType, REQUIRED_ARTIFACT_TYPES } from "../models/artifact-set.js";

export interface ArtifactRepository {
  upsertArtifactSet(artifactSet: ArtifactSet): ArtifactSet;
  listArtifactSets(requestId: string): ArtifactSet[];
  getArtifactSet(requestId: string, domainKey: string): ArtifactSet | undefined;
  getArtifact(requestId: string, domainKey: string, artifactType: ArtifactType): Artifact | undefined;
  markValidated(requestId: string, validatedAt: string): ArtifactSet[];
}

export class InMemoryArtifactRepository implements ArtifactRepository {
  private readonly artifactSets = new Map<string, ArtifactSet>();

  upsertArtifactSet(artifactSet: ArtifactSet): ArtifactSet {
    const key = artifactSetKey(artifactSet.scopeRequestId, artifactSet.domainKey);
    const existing = this.artifactSets.get(key);
    const next = existing?.contentFingerprint === artifactSet.contentFingerprint ? { ...artifactSet, id: existing.id, generatedAt: existing.generatedAt } : artifactSet;
    this.artifactSets.set(key, next);
    return next;
  }

  listArtifactSets(requestId: string): ArtifactSet[] {
    return [...this.artifactSets.values()]
      .filter((artifactSet) => artifactSet.scopeRequestId === requestId)
      .sort((left, right) => left.domainKey.localeCompare(right.domainKey));
  }

  getArtifactSet(requestId: string, domainKey: string): ArtifactSet | undefined {
    return this.artifactSets.get(artifactSetKey(requestId, domainKey));
  }

  getArtifact(requestId: string, domainKey: string, artifactType: ArtifactType): Artifact | undefined {
    return this.getArtifactSet(requestId, domainKey)?.artifacts.find((artifact) => artifact.artifactType === artifactType);
  }

  markValidated(requestId: string, validatedAt: string): ArtifactSet[] {
    const updated: ArtifactSet[] = [];
    for (const artifactSet of this.listArtifactSets(requestId)) {
      const next: ArtifactSet = {
        ...artifactSet,
        status: artifactSet.status === "complete" ? "validated" : artifactSet.status,
        validatedAt,
        artifacts: artifactSet.artifacts.map((artifact) => ({
          ...artifact,
          status: artifact.status === "generated" ? "validated" : artifact.status
        }))
      };
      this.artifactSets.set(artifactSetKey(requestId, artifactSet.domainKey), next);
      updated.push(next);
    }
    return updated;
  }
}

export function artifactSetKey(requestId: string, domainKey: string): string {
  return `${requestId}:${domainKey}`;
}

export function computeArtifactSetStatus(artifacts: Artifact[]): ArtifactSetStatus {
  const statuses = new Map(artifacts.map((artifact) => [artifact.artifactType, artifact.status]));
  if (REQUIRED_ARTIFACT_TYPES.some((artifactType) => !statuses.has(artifactType))) {
    return "incomplete";
  }
  if ([...statuses.values()].some((status) => status === "error")) {
    return "error";
  }
  if ([...statuses.values()].some((status) => status === "incomplete")) {
    return "incomplete";
  }
  if ([...statuses.values()].every((status) => status === "validated")) {
    return "validated";
  }
  return "complete";
}

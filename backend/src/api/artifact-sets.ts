import { FastifyInstance } from "fastify";
import { ArtifactType } from "../models/artifact-set.js";
import { ArtifactRepository, computeArtifactSetStatus } from "../repositories/artifact-repository.js";
import { ScopeRequestRepository } from "../repositories/scope-request-repository.js";
import { ArtifactGenerator } from "../services/artifact-generator.js";
import { GenerationPlanner } from "../services/generation-planner.js";
import { ScopeResolver } from "../services/scope-resolver.js";
import { notFound } from "./errors.js";

export interface ArtifactRouteDependencies {
  scopeRequests: ScopeRequestRepository;
  artifacts: ArtifactRepository;
  resolver: ScopeResolver;
  planner: GenerationPlanner;
  generator: ArtifactGenerator;
}

export function registerArtifactRoutes(app: FastifyInstance, dependencies: ArtifactRouteDependencies, basePath = "") {
  const scopeRequestsPath = (suffix = "") => `${basePath}/scope-requests${suffix}`;

  app.post(scopeRequestsPath("/:requestId/generate-artifacts"), async (request, reply) => {
    const { requestId } = request.params as { requestId: string };
    const scopeRequest = dependencies.scopeRequests.get(requestId);
    if (!scopeRequest) {
      throw notFound("Scope request");
    }

    let domains = dependencies.scopeRequests.getDomains(requestId);
    if (domains.length === 0) {
      const resolution = dependencies.resolver.resolve(scopeRequest);
      domains = dependencies.scopeRequests.saveDomains(requestId, dependencies.planner.assignStages(resolution.domains), resolution.assumptions);
    }

    const assumptions = dependencies.scopeRequests.getAssumptions(requestId);
    const generated = dependencies.generator.generate(scopeRequest, domains, assumptions).map((artifactSet) => dependencies.artifacts.upsertArtifactSet(artifactSet));
    dependencies.scopeRequests.saveDomains(
      requestId,
      domains.map((domain) => ({ ...domain, status: "generated" })),
      assumptions
    );
    dependencies.scopeRequests.updateStatus(requestId, "generated");

    reply.status(202).send({
      requestId,
      artifactSets: generated.map((artifactSet) => ({
        ...artifactSet,
        status: computeArtifactSetStatus(artifactSet.artifacts)
      }))
    });
  });

  app.get(scopeRequestsPath("/:requestId/artifact-sets"), async (request) => {
    const { requestId } = request.params as { requestId: string };
    if (!dependencies.scopeRequests.get(requestId)) {
      throw notFound("Scope request");
    }
    return {
      items: dependencies.artifacts.listArtifactSets(requestId).map((artifactSet) => ({
        ...artifactSet,
        status: computeArtifactSetStatus(artifactSet.artifacts)
      }))
    };
  });

  app.get(scopeRequestsPath("/:requestId/artifact-sets/:domainKey/artifacts/:artifactType"), async (request) => {
    const { requestId, domainKey, artifactType } = request.params as { requestId: string; domainKey: string; artifactType: ArtifactType };
    if (!dependencies.scopeRequests.get(requestId)) {
      throw notFound("Scope request");
    }
    const artifact = dependencies.artifacts.getArtifact(requestId, domainKey, artifactType);
    if (!artifact) {
      throw notFound("Artifact");
    }
    return artifact;
  });
}

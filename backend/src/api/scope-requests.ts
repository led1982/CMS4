import { FastifyInstance } from "fastify";
import { GenerationPlanner } from "../services/generation-planner.js";
import { ScopeResolver } from "../services/scope-resolver.js";
import { ScopeRequestRepository, toGenerationOrder } from "../repositories/scope-request-repository.js";
import { ArtifactRepository, computeArtifactSetStatus } from "../repositories/artifact-repository.js";
import { ValidationRepository } from "../repositories/validation-repository.js";
import { ScopeRequestCreate, ScopeRequestStatus } from "../models/scope-request.js";
import { badRequest, notFound } from "./errors.js";

export interface ScopeRouteDependencies {
  scopeRequests: ScopeRequestRepository;
  artifacts: ArtifactRepository;
  validations: ValidationRepository;
  resolver: ScopeResolver;
  planner: GenerationPlanner;
}

export function registerScopeRequestRoutes(app: FastifyInstance, dependencies: ScopeRouteDependencies, basePath = "") {
  const scopeRequestsPath = (suffix = "") => `${basePath}/scope-requests${suffix}`;

  app.post(scopeRequestsPath(), async (request, reply) => {
    try {
      const created = dependencies.scopeRequests.create(request.body as ScopeRequestCreate);
      reply.status(201).send(created);
    } catch (error) {
      throw badRequest(error instanceof Error ? error.message : "Invalid scope request.");
    }
  });

  app.get(scopeRequestsPath(), async (request) => {
    const query = request.query as { status?: ScopeRequestStatus };
    const status = query.status;
    return {
      items: dependencies.scopeRequests.listSummaries(
        (requestId) => ({
          effectiveDomainCount: dependencies.scopeRequests.getDomains(requestId).length,
          artifactSetCount: dependencies.artifacts.listArtifactSets(requestId).length,
          validationStatus: dependencies.validations.latestReport(requestId).status
        }),
        status
      )
    };
  });

  app.get(scopeRequestsPath("/:requestId"), async (request) => {
    const { requestId } = request.params as { requestId: string };
    const scopeRequest = dependencies.scopeRequests.get(requestId);
    if (!scopeRequest) {
      throw notFound("Scope request");
    }
    return {
      request: scopeRequest,
      domains: dependencies.scopeRequests.getDomains(requestId),
      artifactSets: dependencies.artifacts.listArtifactSets(requestId).map((artifactSet) => ({
        ...artifactSet,
        status: computeArtifactSetStatus(artifactSet.artifacts)
      })),
      latestValidation: dependencies.validations.latestReport(requestId),
      assumptions: dependencies.scopeRequests.getAssumptions(requestId)
    };
  });

  app.post(scopeRequestsPath("/:requestId/resolve-domains"), async (request) => {
    const { requestId } = request.params as { requestId: string };
    const scopeRequest = dependencies.scopeRequests.get(requestId);
    if (!scopeRequest) {
      throw notFound("Scope request");
    }
    const resolution = dependencies.resolver.resolve(scopeRequest);
    const planned = dependencies.planner.assignStages(resolution.domains);
    const domains = dependencies.scopeRequests.saveDomains(requestId, planned, resolution.assumptions);
    dependencies.scopeRequests.updateStatus(requestId, "resolved");
    return {
      requestId,
      domains,
      assumptions: resolution.assumptions
    };
  });

  app.put(scopeRequestsPath("/:requestId/generation-order"), async (request) => {
    const { requestId } = request.params as { requestId: string };
    if (!dependencies.scopeRequests.get(requestId)) {
      throw notFound("Scope request");
    }
    const body = request.body as { stages?: Array<{ stageNumber: number; domainKeys: string[] }> };
    if (!Array.isArray(body.stages) || body.stages.length === 0) {
      throw badRequest("stages must include at least one generation stage.");
    }
    try {
      return dependencies.scopeRequests.replaceGenerationOrder(requestId, { stages: body.stages.map((stage) => ({ ...stage, status: "ready" as const })) });
    } catch (error) {
      throw badRequest(error instanceof Error ? error.message : "Invalid generation order.");
    }
  });

  app.get(scopeRequestsPath("/:requestId/generation-order"), async (request) => {
    const { requestId } = request.params as { requestId: string };
    if (!dependencies.scopeRequests.get(requestId)) {
      throw notFound("Scope request");
    }
    return toGenerationOrder(dependencies.scopeRequests.getDomains(requestId), "ready");
  });
}

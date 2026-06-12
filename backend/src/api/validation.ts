import { FastifyInstance } from "fastify";
import { ArtifactRepository } from "../repositories/artifact-repository.js";
import { ScopeRequestRepository } from "../repositories/scope-request-repository.js";
import { ValidationRepository } from "../repositories/validation-repository.js";
import { DependencyValidator } from "../services/dependency-validator.js";
import { conflict, notFound } from "./errors.js";

export interface ValidationRouteDependencies {
  scopeRequests: ScopeRequestRepository;
  artifacts: ArtifactRepository;
  validations: ValidationRepository;
  validator: DependencyValidator;
}

export function registerValidationRoutes(app: FastifyInstance, dependencies: ValidationRouteDependencies, basePath = "") {
  const scopeRequestsPath = (suffix = "") => `${basePath}/scope-requests${suffix}`;

  app.post(scopeRequestsPath("/:requestId/validate"), async (request) => {
    const { requestId } = request.params as { requestId: string };
    const scopeRequest = dependencies.scopeRequests.get(requestId);
    if (!scopeRequest) {
      throw notFound("Scope request");
    }
    const domains = dependencies.scopeRequests.getDomains(requestId);
    if (domains.length === 0) {
      throw conflict("Resolve domains before validation.");
    }

    const report = dependencies.validator.validate(domains, dependencies.artifacts.listArtifactSets(requestId));
    dependencies.validations.saveRun(requestId, report);

    if (report.status === "passed" || report.status === "passed_with_warnings") {
      const validatedAt = new Date().toISOString();
      dependencies.artifacts.markValidated(requestId, validatedAt);
      dependencies.scopeRequests.saveDomains(
        requestId,
        domains.map((domain) => ({ ...domain, status: "validated" })),
        dependencies.scopeRequests.getAssumptions(requestId)
      );
      dependencies.scopeRequests.updateStatus(requestId, "validated");
    } else {
      dependencies.scopeRequests.updateStatus(requestId, "blocked");
    }

    return report;
  });
}

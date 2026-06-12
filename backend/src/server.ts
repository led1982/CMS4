import Fastify from "fastify";
import { registerArtifactRoutes } from "./api/artifact-sets.js";
import { registerErrorHandling } from "./api/errors.js";
import { registerOpenApiValidation } from "./api/openapi-validation.js";
import { registerScopeRequestRoutes } from "./api/scope-requests.js";
import { registerValidationRoutes } from "./api/validation.js";
import { InMemoryArtifactRepository } from "./repositories/artifact-repository.js";
import { InMemoryScopeRequestRepository } from "./repositories/scope-request-repository.js";
import { InMemoryValidationRepository } from "./repositories/validation-repository.js";
import { ArtifactGenerator } from "./services/artifact-generator.js";
import { DependencyValidator } from "./services/dependency-validator.js";
import { GenerationPlanner } from "./services/generation-planner.js";
import { ScopeResolver } from "./services/scope-resolver.js";

export function buildApp() {
  const app = Fastify({ logger: process.env.NODE_ENV !== "test" });
  const scopeRequests = new InMemoryScopeRequestRepository();
  const artifacts = new InMemoryArtifactRepository();
  const validations = new InMemoryValidationRepository();
  const resolver = new ScopeResolver();
  const planner = new GenerationPlanner();
  const generator = new ArtifactGenerator();
  const validator = new DependencyValidator();
  const routeDependencies = { scopeRequests, artifacts, validations, resolver, planner, generator, validator };

  app.addHook("onRequest", async (request, reply) => {
    reply.header("Access-Control-Allow-Origin", process.env.CORS_ALLOWED_ORIGINS ?? "*");
    reply.header("Access-Control-Allow-Headers", "Content-Type, X-CMS-User");
    reply.header("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
    if (request.method === "OPTIONS") {
      reply.status(204).send();
    }
  });

  app.get("/health", async () => ({ status: "ok" }));
  registerOpenApiValidation(app);
  registerScopeRequestRoutes(app, routeDependencies);
  registerArtifactRoutes(app, routeDependencies);
  registerValidationRoutes(app, routeDependencies);
  registerScopeRequestRoutes(app, routeDependencies, "/api");
  registerArtifactRoutes(app, routeDependencies, "/api");
  registerValidationRoutes(app, routeDependencies, "/api");
  registerErrorHandling(app);

  return app;
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const app = buildApp();
  const port = Number(process.env.PORT ?? 3000);
  const host = process.env.HOST ?? "0.0.0.0";
  await app.listen({ port, host });
}

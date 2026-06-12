import { FastifyInstance } from "fastify";
import { badRequest } from "./errors.js";

const routesWithJsonBodies = new Set([
  "POST /scope-requests",
  "PUT /scope-requests/:requestId/generation-order",
  "POST /api/scope-requests",
  "PUT /api/scope-requests/:requestId/generation-order"
]);

export function registerOpenApiValidation(app: FastifyInstance) {
  app.addHook("preValidation", async (request) => {
    const routeKey = `${request.method} ${request.routeOptions.url}`;
    if (!routesWithJsonBodies.has(routeKey)) {
      return;
    }
    if (typeof request.body !== "object" || request.body === null) {
      throw badRequest("Request body must be a JSON object.");
    }
  });
}

import { FastifyError, FastifyInstance, FastifyReply, FastifyRequest } from "fastify";

export class ApiError extends Error {
  constructor(
    public readonly statusCode: number,
    public readonly code: string,
    message: string,
    public readonly details?: Record<string, unknown>
  ) {
    super(message);
  }
}

export function badRequest(message: string, details?: Record<string, unknown>) {
  return new ApiError(400, "BAD_REQUEST", message, details);
}

export function conflict(message: string, details?: Record<string, unknown>) {
  return new ApiError(409, "CONFLICT", message, details);
}

export function notFound(resource: string) {
  return new ApiError(404, "NOT_FOUND", `${resource} was not found.`);
}

export function registerErrorHandling(app: FastifyInstance) {
  app.setNotFoundHandler((_request, reply) => {
    reply.status(404).send({ code: "NOT_FOUND", message: "Route was not found." });
  });

  app.setErrorHandler((error: FastifyError | ApiError, _request: FastifyRequest, reply: FastifyReply) => {
    if (error instanceof ApiError) {
      reply.status(error.statusCode).send({
        code: error.code,
        message: error.message,
        details: error.details
      });
      return;
    }

    const message = error.message || "Unexpected server error.";
    const statusCode = typeof error.statusCode === "number" ? error.statusCode : 500;
    reply.status(statusCode).send({
      code: statusCode >= 500 ? "INTERNAL_ERROR" : "BAD_REQUEST",
      message
    });
  });
}

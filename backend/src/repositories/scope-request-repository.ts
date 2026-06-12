import { randomUUID } from "node:crypto";
import { Domain, GenerationOrder, GenerationStageStatus } from "../models/domain.js";
import { normalizeScopeRequest, ScopeRequest, ScopeRequestCreate, ScopeRequestStatus, ScopeRequestSummary } from "../models/scope-request.js";

export interface ScopeRequestRepository {
  create(input: ScopeRequestCreate): ScopeRequest;
  list(status?: ScopeRequestStatus): ScopeRequest[];
  get(requestId: string): ScopeRequest | undefined;
  updateStatus(requestId: string, status: ScopeRequestStatus): ScopeRequest;
  saveDomains(requestId: string, domains: Domain[], assumptions: string[]): Domain[];
  getDomains(requestId: string): Domain[];
  getAssumptions(requestId: string): string[];
  replaceGenerationOrder(requestId: string, order: GenerationOrder): GenerationOrder;
  listSummaries(countsFor: (requestId: string) => { effectiveDomainCount: number; artifactSetCount: number; validationStatus: ScopeRequestSummary["validationStatus"] }, status?: ScopeRequestStatus): ScopeRequestSummary[];
}

export class InMemoryScopeRequestRepository implements ScopeRequestRepository {
  private readonly requests = new Map<string, ScopeRequest>();
  private readonly domains = new Map<string, Domain[]>();
  private readonly assumptions = new Map<string, string[]>();

  create(input: ScopeRequestCreate): ScopeRequest {
    const normalized = normalizeScopeRequest(input);
    validateScopeRequestCreate(normalized);

    const now = new Date().toISOString();
    const request: ScopeRequest = {
      id: randomUUID(),
      requestCode: normalized.requestCode,
      slug: normalized.slug,
      requestType: normalized.requestType,
      title: normalized.title,
      goal: normalized.goal,
      selectedDomains: normalized.selectedDomains ?? [],
      autoAddedDomains: normalized.autoAddedDomains ?? [],
      declaredEffectiveDomains: normalized.declaredEffectiveDomains ?? [],
      dependencies: normalized.dependencies ?? [],
      acceptanceCriteria: normalized.acceptanceCriteria,
      sourceText: normalized.sourceText ?? "",
      status: "draft",
      createdAt: now,
      updatedAt: now
    };

    this.requests.set(request.id, request);
    this.domains.set(request.id, []);
    this.assumptions.set(request.id, []);
    return request;
  }

  list(status?: ScopeRequestStatus): ScopeRequest[] {
    return [...this.requests.values()]
      .filter((request) => !status || request.status === status)
      .sort((left, right) => right.updatedAt.localeCompare(left.updatedAt));
  }

  get(requestId: string): ScopeRequest | undefined {
    return this.requests.get(requestId);
  }

  updateStatus(requestId: string, status: ScopeRequestStatus): ScopeRequest {
    const request = this.requireRequest(requestId);
    const updated = { ...request, status, updatedAt: new Date().toISOString() };
    this.requests.set(requestId, updated);
    return updated;
  }

  saveDomains(requestId: string, domains: Domain[], assumptions: string[]): Domain[] {
    this.requireRequest(requestId);
    const unique = new Map<string, Domain>();
    for (const domain of domains) {
      unique.set(domain.domainKey, domain);
    }
    const normalized = [...unique.values()].sort((left, right) => left.generationStage - right.generationStage || left.domainKey.localeCompare(right.domainKey));
    this.domains.set(requestId, normalized);
    this.assumptions.set(requestId, [...new Set(assumptions)]);
    return normalized;
  }

  getDomains(requestId: string): Domain[] {
    this.requireRequest(requestId);
    return [...(this.domains.get(requestId) ?? [])];
  }

  getAssumptions(requestId: string): string[] {
    this.requireRequest(requestId);
    return [...(this.assumptions.get(requestId) ?? [])];
  }

  replaceGenerationOrder(requestId: string, order: GenerationOrder): GenerationOrder {
    const currentDomains = this.getDomains(requestId);
    const byKey = new Map(currentDomains.map((domain) => [domain.domainKey, domain]));
    const assigned = new Set<string>();

    for (const stage of order.stages) {
      if (!Number.isInteger(stage.stageNumber) || stage.stageNumber < 1) {
        throw new Error("stageNumber must be a positive integer.");
      }
      for (const domainKey of stage.domainKeys) {
        if (!byKey.has(domainKey)) {
          throw new Error(`Unknown domain '${domainKey}' in generation order.`);
        }
        if (assigned.has(domainKey)) {
          throw new Error(`Domain '${domainKey}' appears in more than one generation stage.`);
        }
        assigned.add(domainKey);
      }
    }

    for (const domain of currentDomains) {
      if (!assigned.has(domain.domainKey)) {
        throw new Error(`Domain '${domain.domainKey}' is missing from generation order.`);
      }
    }

    const updatedDomains = currentDomains.map((domain) => {
      const stage = order.stages.find((candidate) => candidate.domainKeys.includes(domain.domainKey));
      return stage ? { ...domain, generationStage: stage.stageNumber, status: "planned" as const } : domain;
    });
    this.saveDomains(requestId, updatedDomains, this.getAssumptions(requestId));
    return toGenerationOrder(updatedDomains, "ready");
  }

  listSummaries(
    countsFor: (requestId: string) => { effectiveDomainCount: number; artifactSetCount: number; validationStatus: ScopeRequestSummary["validationStatus"] },
    status?: ScopeRequestStatus
  ): ScopeRequestSummary[] {
    return this.list(status).map((request) => ({
      id: request.id,
      requestCode: request.requestCode,
      title: request.title,
      status: request.status,
      ...countsFor(request.id)
    }));
  }

  private requireRequest(requestId: string): ScopeRequest {
    const request = this.requests.get(requestId);
    if (!request) {
      throw new Error(`Scope request '${requestId}' was not found.`);
    }
    return request;
  }
}

export function toGenerationOrder(domains: Domain[], status: GenerationStageStatus): GenerationOrder {
  const stages = new Map<number, string[]>();
  for (const domain of domains) {
    const current = stages.get(domain.generationStage) ?? [];
    current.push(domain.domainKey);
    stages.set(domain.generationStage, current);
  }

  return {
    stages: [...stages.entries()]
      .sort(([left], [right]) => left - right)
      .map(([stageNumber, domainKeys]) => ({
        stageNumber,
        domainKeys: domainKeys.sort(),
        status
      }))
  };
}

function validateScopeRequestCreate(input: ScopeRequestCreate) {
  const missing = ["requestCode", "slug", "requestType", "title", "goal"].filter((field) => !String(input[field as keyof ScopeRequestCreate] ?? "").trim());
  if (missing.length > 0) {
    throw new Error(`Missing required field(s): ${missing.join(", ")}.`);
  }
  if (!["new", "change", "fix"].includes(input.requestType)) {
    throw new Error("requestType must be one of new, change, or fix.");
  }
  if ((input.acceptanceCriteria ?? []).length === 0) {
    throw new Error("acceptanceCriteria must include at least one item.");
  }
}

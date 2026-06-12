import { randomUUID } from "node:crypto";
import { Domain, DomainSource, displayNameForDomain } from "../models/domain.js";
import { normalizeDomainKey, ScopeRequest } from "../models/scope-request.js";

export interface DomainResolution {
  domains: Domain[];
  assumptions: string[];
}

const DEFAULT_CMS_DOMAIN = "cms-core";

export class ScopeResolver {
  resolve(request: ScopeRequest): DomainResolution {
    const assumptions: string[] = [];
    const collected = new Map<string, DomainSource>();

    addDomains(collected, request.selectedDomains, "selected");
    addDomains(collected, request.autoAddedDomains, "auto_added");
    addDomains(collected, request.declaredEffectiveDomains, "declared_effective");

    if (collected.size === 0) {
      collected.set(DEFAULT_CMS_DOMAIN, "default");
      assumptions.push("Empty CMS input defaulted to cms-core.");
    }

    const dependencyMap = new Map(request.dependencies.map((dependency) => [dependency.domainKey, dependency.requires]));
    const domains = [...collected.entries()].map(([domainKey, source]) => ({
      id: randomUUID(),
      scopeRequestId: request.id,
      domainKey,
      displayName: displayNameForDomain(domainKey),
      source,
      requires: dependencyMap.get(domainKey) ?? [],
      generationStage: 1,
      description: `${displayNameForDomain(domainKey)} planning domain for ${request.requestCode}.`,
      status: "pending" as const
    }));

    return {
      domains: domains.sort((left, right) => left.domainKey.localeCompare(right.domainKey)),
      assumptions
    };
  }
}

function addDomains(target: Map<string, DomainSource>, values: string[], source: DomainSource) {
  for (const value of values) {
    const domainKey = normalizeDomainKey(value);
    if (domainKey && !target.has(domainKey)) {
      target.set(domainKey, source);
    }
  }
}

import { Domain } from "../models/domain.js";

export class GenerationPlanner {
  assignStages(domains: Domain[]): Domain[] {
    const domainKeys = new Set(domains.map((domain) => domain.domainKey));
    const planned = new Map<string, number>();

    const stageFor = (domain: Domain, visiting: Set<string>): number => {
      const existing = planned.get(domain.domainKey);
      if (existing) {
        return existing;
      }
      if (visiting.has(domain.domainKey)) {
        planned.set(domain.domainKey, 1);
        return 1;
      }

      visiting.add(domain.domainKey);
      const dependencyStages = domain.requires
        .filter((dependency) => domainKeys.has(dependency))
        .map((dependency) => {
          const dependencyDomain = domains.find((candidate) => candidate.domainKey === dependency);
          return dependencyDomain ? stageFor(dependencyDomain, visiting) : 1;
        });
      visiting.delete(domain.domainKey);

      const stage = dependencyStages.length === 0 ? 1 : Math.max(...dependencyStages);
      planned.set(domain.domainKey, stage);
      return stage;
    };

    return domains.map((domain) => ({
      ...domain,
      generationStage: stageFor(domain, new Set()),
      status: "planned" as const
    }));
  }
}

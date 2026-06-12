import { ArtifactSet, REQUIRED_ARTIFACT_TYPES } from "../models/artifact-set.js";
import { Domain } from "../models/domain.js";
import { ValidationFinding } from "../models/validation-finding.js";
import { reportFromFindings } from "../repositories/validation-repository.js";

export class DependencyValidator {
  validate(domains: Domain[], artifactSets: ArtifactSet[]) {
    const findings: ValidationFinding[] = [];
    findings.push(...validateDuplicateDomains(domains));
    findings.push(...validateDependencies(domains));
    findings.push(...validateCycles(domains));
    findings.push(...validateArtifacts(domains, artifactSets));
    return reportFromFindings(findings);
  }
}

function validateDuplicateDomains(domains: Domain[]): ValidationFinding[] {
  const findings: ValidationFinding[] = [];
  const seen = new Set<string>();
  for (const domain of domains) {
    if (seen.has(domain.domainKey)) {
      findings.push({
        severity: "error",
        findingType: "duplicate_domain",
        domainKey: domain.domainKey,
        message: `Domain '${domain.domainKey}' appears more than once.`,
        recommendedAction: "Merge duplicate domain inputs into one effective domain."
      });
    }
    seen.add(domain.domainKey);
  }
  return findings;
}

function validateDependencies(domains: Domain[]): ValidationFinding[] {
  const findings: ValidationFinding[] = [];
  const byKey = new Map(domains.map((domain) => [domain.domainKey, domain]));

  for (const domain of domains) {
    for (const dependencyKey of domain.requires) {
      if (dependencyKey === domain.domainKey) {
        findings.push({
          severity: "error",
          findingType: "self_dependency",
          domainKey: domain.domainKey,
          relatedDomainKey: dependencyKey,
          message: `Domain '${domain.domainKey}' requires itself.`,
          recommendedAction: "Remove the self dependency or split the domain into separate stages."
        });
        continue;
      }

      const dependency = byKey.get(dependencyKey);
      if (!dependency) {
        findings.push({
          severity: "error",
          findingType: "missing_dependency",
          domainKey: domain.domainKey,
          relatedDomainKey: dependencyKey,
          message: `Domain '${domain.domainKey}' requires missing domain '${dependencyKey}'.`,
          recommendedAction: "Add the required domain to the request or remove the dependency."
        });
        continue;
      }

      if (dependency.generationStage > domain.generationStage) {
        findings.push({
          severity: "error",
          findingType: "later_stage_dependency",
          domainKey: domain.domainKey,
          relatedDomainKey: dependencyKey,
          message: `Domain '${domain.domainKey}' is in stage ${domain.generationStage}, but dependency '${dependencyKey}' is in later stage ${dependency.generationStage}.`,
          recommendedAction: `Move '${dependencyKey}' to stage ${domain.generationStage} or earlier.`
        });
      }
    }
  }

  return findings;
}

function validateCycles(domains: Domain[]): ValidationFinding[] {
  const findings: ValidationFinding[] = [];
  const byKey = new Map(domains.map((domain) => [domain.domainKey, domain]));
  const visited = new Set<string>();
  const active = new Set<string>();
  const reported = new Set<string>();

  const visit = (domainKey: string, path: string[]) => {
    if (active.has(domainKey)) {
      const cycle = path.slice(path.indexOf(domainKey)).concat(domainKey);
      for (const key of new Set(cycle)) {
        if (!reported.has(key)) {
          reported.add(key);
          findings.push({
            severity: "error",
            findingType: "cycle",
            domainKey: key,
            message: `Circular dependency detected: ${cycle.join(" -> ")}.`,
            recommendedAction: "Break the cycle by removing or reordering at least one dependency."
          });
        }
      }
      return;
    }
    if (visited.has(domainKey)) {
      return;
    }

    const domain = byKey.get(domainKey);
    if (!domain) {
      return;
    }

    active.add(domainKey);
    for (const dependencyKey of domain.requires) {
      visit(dependencyKey, [...path, dependencyKey]);
    }
    active.delete(domainKey);
    visited.add(domainKey);
  };

  for (const domain of domains) {
    visit(domain.domainKey, [domain.domainKey]);
  }

  return findings;
}

function validateArtifacts(domains: Domain[], artifactSets: ArtifactSet[]): ValidationFinding[] {
  const findings: ValidationFinding[] = [];
  const artifactSetByDomain = new Map(artifactSets.map((artifactSet) => [artifactSet.domainKey, artifactSet]));

  for (const domain of domains) {
    const artifactSet = artifactSetByDomain.get(domain.domainKey);
    if (!artifactSet) {
      findings.push({
        severity: "error",
        findingType: "missing_artifact",
        domainKey: domain.domainKey,
        message: `Domain '${domain.domainKey}' has no artifact set.`,
        recommendedAction: "Generate artifacts for the domain before validation."
      });
      continue;
    }

    for (const artifactType of REQUIRED_ARTIFACT_TYPES) {
      const artifact = artifactSet.artifacts.find((candidate) => candidate.artifactType === artifactType);
      if (!artifact || !["generated", "validated"].includes(artifact.status)) {
        findings.push({
          severity: "error",
          findingType: "missing_artifact",
          domainKey: domain.domainKey,
          message: `Domain '${domain.domainKey}' is missing ${artifactType}.md.`,
          recommendedAction: `Regenerate ${artifactType}.md for ${domain.domainKey}.`
        });
      }
    }
  }

  return findings;
}

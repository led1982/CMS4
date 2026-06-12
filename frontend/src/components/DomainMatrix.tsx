import type { Domain } from "../services/scopeRequestClient";

export function DomainMatrix({ domains }: { domains: Domain[] }) {
  const stages = [...new Set(domains.map((domain) => domain.generationStage))].sort((left, right) => left - right);

  return (
    <section>
      <div className="section-heading">
        <h2>Domain Matrix</h2>
      </div>
      {domains.length === 0 ? (
        <p className="muted-text">No effective domains have been resolved.</p>
      ) : (
        <div className="domain-stage-grid">
          {stages.map((stage) => (
            <div className="domain-stage" key={stage}>
              <h3>Stage {stage}</h3>
              {domains
                .filter((domain) => domain.generationStage === stage)
                .map((domain) => (
                  <article className="domain-row" key={domain.domainKey}>
                    <strong>{domain.displayName}</strong>
                    <span>{domain.domainKey}</span>
                    <span className={`status-pill status-${domain.status}`}>{domain.status}</span>
                    <small>{domain.requires.length === 0 ? "No dependencies" : `Requires ${domain.requires.join(", ")}`}</small>
                  </article>
                ))}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

import type { Domain } from "../services/scopeRequestClient";

export function GenerationOrderTimeline({ domains }: { domains: Domain[] }) {
  const stages = [...new Set(domains.map((domain) => domain.generationStage))].sort((left, right) => left - right);

  return (
    <section>
      <div className="section-heading">
        <h2>Generation Order</h2>
      </div>
      <ol className="timeline-list">
        {stages.map((stage) => (
          <li key={stage}>
            <strong>Stage {stage}</strong>
            <span>{domains.filter((domain) => domain.generationStage === stage).map((domain) => domain.domainKey).join(", ")}</span>
          </li>
        ))}
      </ol>
    </section>
  );
}

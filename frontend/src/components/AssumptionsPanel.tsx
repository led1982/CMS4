export function AssumptionsPanel({ assumptions }: { assumptions: string[] }) {
  return (
    <section>
      <div className="section-heading">
        <h2>Assumptions</h2>
      </div>
      {assumptions.length === 0 ? (
        <p className="muted-text">No default assumptions have been applied.</p>
      ) : (
        <ul className="compact-list">
          {assumptions.map((assumption) => (
            <li key={assumption}>{assumption}</li>
          ))}
        </ul>
      )}
    </section>
  );
}

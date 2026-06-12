import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { RequestDetailPage } from "../../src/pages/RequestDetailPage";

describe("RequestDetailPage", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () =>
        new Response(
          JSON.stringify({
            request: {
              id: "request-1",
              requestCode: "CMS4-4",
              slug: "cms4",
              requestType: "new",
              title: "cms",
              goal: "cms",
              selectedDomains: [],
              autoAddedDomains: [],
              declaredEffectiveDomains: [],
              dependencies: [],
              acceptanceCriteria: ["generate draft artifacts"],
              sourceText: "SPEC-REQUEST.md",
              status: "generated",
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString()
            },
            domains: [
              {
                domainKey: "cms-core",
                displayName: "CMS Core",
                source: "default",
                requires: [],
                generationStage: 1,
                status: "generated"
              }
            ],
            artifactSets: [
              {
                domainKey: "cms-core",
                status: "complete",
                generatedAt: new Date().toISOString(),
                artifacts: [
                  { artifactType: "spec", path: "cms-core/spec.md", status: "generated" },
                  { artifactType: "plan", path: "cms-core/plan.md", status: "generated" },
                  { artifactType: "tasks", path: "cms-core/tasks.md", status: "generated" }
                ]
              }
            ],
            latestValidation: { status: "passed", errorCount: 0, warningCount: 0, findings: [] },
            assumptions: ["Empty CMS input defaulted to cms-core."]
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      )
    );
  });

  it("shows artifact coverage and default-domain assumption", async () => {
    render(
      <MemoryRouter initialEntries={["/scope-requests/request-1"]}>
        <Routes>
          <Route path="/scope-requests/:requestId" element={<RequestDetailPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByText("CMS4-4 / cms")).toBeInTheDocument();
    expect(screen.getByText("Empty CMS input defaulted to cms-core.")).toBeInTheDocument();
    expect(screen.getByText("spec.md")).toBeInTheDocument();
    expect(screen.getByText("plan.md")).toBeInTheDocument();
    expect(screen.getByText("tasks.md")).toBeInTheDocument();
  });
});

import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ArtifactPreviewPage } from "../../src/pages/ArtifactPreviewPage";

describe("ArtifactPreviewPage", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes("/artifacts/plan")) {
          return jsonResponse({ artifactType: "plan", path: "cms-core/plan.md", status: "generated", title: "plan.md", content: "# Implementation Plan: cms" });
        }
        if (url.includes("/artifacts/tasks")) {
          return jsonResponse({ artifactType: "tasks", path: "cms-core/tasks.md", status: "generated", title: "tasks.md", content: "# Tasks: cms" });
        }
        if (url.includes("/artifacts/spec")) {
          return jsonResponse({ artifactType: "spec", path: "cms-core/spec.md", status: "generated", title: "spec.md", content: "# Feature Specification: cms" });
        }
        return jsonResponse({
          request: { id: "request-1", requestCode: "CMS4-4", title: "cms", goal: "cms" },
          domains: [{ domainKey: "cms-core", displayName: "CMS Core", source: "default", requires: [], generationStage: 1, status: "generated" }],
          artifactSets: [],
          latestValidation: { status: "passed", errorCount: 0, warningCount: 0, findings: [] },
          assumptions: []
        });
      })
    );
  });

  it("switches artifact preview tabs", async () => {
    render(
      <MemoryRouter initialEntries={["/scope-requests/request-1/artifacts/cms-core?type=spec"]}>
        <Routes>
          <Route path="/scope-requests/:requestId/artifacts/:domainKey" element={<ArtifactPreviewPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByText("Feature Specification: cms")).toBeInTheDocument();
    await userEvent.click(screen.getByRole("tab", { name: "plan.md" }));
    expect(await screen.findByText("Implementation Plan: cms")).toBeInTheDocument();
  });
});

function jsonResponse(payload: unknown) {
  return new Response(JSON.stringify(payload), { status: 200, headers: { "Content-Type": "application/json" } });
}

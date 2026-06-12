import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ArtifactPreviewPage } from "../../src/pages/ArtifactPreviewPage";
import { ValidationPanel } from "../../src/components/ValidationPanel";

describe("status accessibility", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes("/artifacts/")) {
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

  it("renders status text and keyboard-focusable tabs", async () => {
    render(
      <MemoryRouter initialEntries={["/scope-requests/request-1/artifacts/cms-core?type=spec"]}>
        <ValidationPanel report={{ status: "passed", errorCount: 0, warningCount: 0, findings: [] }} />
        <Routes>
          <Route path="/scope-requests/:requestId/artifacts/:domainKey" element={<ArtifactPreviewPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText("passed")).toBeInTheDocument();
    expect(await screen.findByRole("tab", { name: "spec.md" })).toBeInTheDocument();
    await userEvent.tab();
    expect(screen.getByRole("link", { name: "Back to Detail" })).toHaveFocus();
    await userEvent.tab();
    expect(screen.getByLabelText("Domain")).toHaveFocus();
  });
});

function jsonResponse(payload: unknown) {
  return new Response(JSON.stringify(payload), { status: 200, headers: { "Content-Type": "application/json" } });
}

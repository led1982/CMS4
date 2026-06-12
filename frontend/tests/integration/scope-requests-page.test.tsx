import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ScopeRequestsPage } from "../../src/pages/ScopeRequestsPage";

describe("ScopeRequestsPage", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async () =>
        new Response(
          JSON.stringify({
            items: [
              {
                id: "request-1",
                requestCode: "CMS4-4",
                title: "cms",
                status: "generated",
                effectiveDomainCount: 1,
                artifactSetCount: 1,
                validationStatus: "passed"
              }
            ]
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      )
    );
  });

  it("shows request summary coverage", async () => {
    render(
      <MemoryRouter initialEntries={["/scope-requests"]}>
        <Routes>
          <Route path="/scope-requests" element={<ScopeRequestsPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByText("CMS4-4")).toBeInTheDocument();
    expect(screen.getByText("cms")).toBeInTheDocument();
    expect(screen.getByText("passed")).toBeInTheDocument();
  });
});

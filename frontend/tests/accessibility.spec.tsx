import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { AppShell } from "../src/app/AppShell";
import { renderWithRouter, fixtures } from "./test-utils";

describe("accessibility smoke", () => {
  it("exposes labelled navigation and search landmarks", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input);
        return Promise.resolve({
          ok: true,
          status: 200,
          json: async () => (url.includes("/api/v1/me") ? fixtures.user : { requiredNotices: [], pinnedContent: [], recentContent: [], categories: [] })
        });
      })
    );

    renderWithRouter(<AppShell />);

    expect(screen.getByRole("navigation", { name: "Primary" })).toBeInTheDocument();
    expect(screen.getByRole("search")).toBeInTheDocument();
    expect(await screen.findByText("Portal Home")).toBeInTheDocument();
  });
});

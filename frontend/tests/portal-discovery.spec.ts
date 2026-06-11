import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { PortalHome } from "../src/features/portal/PortalHome";
import { renderWithRouter, fixtures } from "./test-utils";

describe("portal discovery", () => {
  it("shows required notices and category entry points", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          requiredNotices: [fixtures.content],
          pinnedContent: [fixtures.content],
          recentContent: [fixtures.content],
          categories: [{ id: "cat-hr", name: "HR Policies", slug: "hr-policies", sortOrder: 10, active: true, children: [] }]
        })
      })
    );

    renderWithRouter(<PortalHome />);

    expect(await screen.findAllByText("Annual Security Awareness Notice")).toHaveLength(3);
    expect(screen.getByText("HR Policies")).toBeInTheDocument();
  });
});

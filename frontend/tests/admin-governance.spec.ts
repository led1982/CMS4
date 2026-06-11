import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { TaxonomyAdmin } from "../src/features/admin/TaxonomyAdmin";
import { renderWithRouter } from "./test-utils";

describe("admin governance", () => {
  it("renders categories and tags for administrators", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn()
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: async () => [{ id: "cat-hr", name: "HR Policies", slug: "hr-policies", sortOrder: 10, active: true, children: [] }]
        })
        .mockResolvedValueOnce({
          ok: true,
          status: 200,
          json: async () => [{ id: "tag-policy", name: "Policy", active: true }]
        })
    );

    renderWithRouter(<TaxonomyAdmin />);

    expect(await screen.findByText("HR Policies")).toBeInTheDocument();
    expect(screen.getByText("Policy")).toBeInTheDocument();
  });
});

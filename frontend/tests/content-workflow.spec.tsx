import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { AuthorWorkspace } from "../src/features/authoring/AuthorWorkspace";
import { renderWithRouter, fixtures } from "./test-utils";

describe("content workflow", () => {
  it("renders editable author workspace rows", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ items: [{ ...fixtures.content, status: "DRAFT" }], page: { page: 0, size: 20, totalItems: 1, totalPages: 1 } })
      })
    );

    renderWithRouter(<AuthorWorkspace />);

    expect(await screen.findByText("Annual Security Awareness Notice")).toBeInTheDocument();
    expect(screen.getByText("Draft")).toBeInTheDocument();
  });
});

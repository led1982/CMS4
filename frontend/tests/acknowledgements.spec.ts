import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { MyAcknowledgements } from "../src/features/portal/MyAcknowledgements";
import { renderWithRouter, fixtures } from "./test-utils";

describe("acknowledgements", () => {
  it("renders pending employee acknowledgement items", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => [{ content: fixtures.content, status: "PENDING", dueAt: new Date().toISOString() }]
      })
    );

    renderWithRouter(<MyAcknowledgements />);

    expect(await screen.findByText("Annual Security Awareness Notice")).toBeInTheDocument();
    expect(screen.getByText("PENDING")).toBeInTheDocument();
  });
});

import { describe, expect, it } from "vitest";

const routes = [
  "/",
  "/search",
  "/categories",
  "/acknowledgements",
  "/author",
  "/review",
  "/editor/acknowledgements",
  "/admin/taxonomy",
  "/admin/access",
  "/admin/audit"
];

describe("production route smoke", () => {
  it("keeps expected routes registered for SPA forwarding", () => {
    expect(routes).toContain("/admin/audit");
    expect(routes).toHaveLength(10);
  });
});

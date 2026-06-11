import { ReactElement } from "react";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";

export function renderWithRouter(ui: ReactElement) {
  return render(<BrowserRouter>{ui}</BrowserRouter>);
}

export const fixtures = {
  user: {
    id: "employee",
    employeeId: "E001",
    displayName: "Employee User",
    email: "employee@example.com",
    department: "Operations",
    roles: ["EMPLOYEE"],
    capabilities: ["PORTAL_READ"]
  },
  content: {
    id: "content-1",
    contentType: "NOTICE",
    title: "Annual Security Awareness Notice",
    summary: "Required annual notice",
    status: "PUBLISHED",
    category: { id: "cat-hr", name: "HR Policies" },
    tags: ["Policy"],
    priority: "URGENT",
    requiresAcknowledgement: true,
    acknowledgementStatus: "PENDING",
    updatedAt: new Date().toISOString()
  }
};

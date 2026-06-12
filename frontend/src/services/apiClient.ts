import { useEffect, useState } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";
const DEFAULT_PERSONA = import.meta.env.VITE_DEFAULT_PERSONA ?? "employee";

export type RoleCode = "EMPLOYEE" | "AUTHOR" | "REVIEWER" | "PUBLISHER" | "NOTICE_MANAGER" | "EDITOR" | "AUDITOR" | "ADMINISTRATOR";
export type ContentType = "KNOWLEDGE_ARTICLE" | "DOCUMENT_RECORD" | "NOTICE";
export type ContentStatus = "DRAFT" | "IN_REVIEW" | "REJECTED" | "SCHEDULED" | "PUBLISHED" | "EXPIRED" | "ARCHIVED" | "DELETED";
export type Priority = "NORMAL" | "PINNED" | "URGENT";
export type AcknowledgementStatus = "NOT_REQUIRED" | "PENDING" | "COMPLETED";

export interface ApiErrorPayload {
  code: string;
  message: string;
  fieldErrors?: Array<{ field: string; message: string }>;
  requestId?: string;
}

export class ApiError extends Error {
  status: number;
  payload: ApiErrorPayload;

  constructor(status: number, payload: ApiErrorPayload) {
    super(payload.message);
    this.status = status;
    this.payload = payload;
  }
}

export interface PageMeta {
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface UserRef {
  id: string;
  displayName: string;
  email?: string;
  department?: string;
}

export interface UserProfile extends UserRef {
  employeeId: string;
  department: string;
  roles: RoleCode[];
  capabilities: string[];
}

export interface Category {
  id: string;
  parentId?: string | null;
  name: string;
  slug: string;
  description?: string;
  ownerUserId?: string;
  defaultAudienceId?: string;
  defaultReviewerGroupId?: string;
  sortOrder: number;
  active: boolean;
  children: Category[];
}

export interface CategoryRef {
  id: string;
  name: string;
}

export interface AudienceRef {
  id: string;
  name: string;
  audienceType: string;
}

export interface AttachmentMetadata {
  id: string;
  fileName: string;
  mimeType: string;
  sizeBytes: number;
  uploadStatus: string;
  validationStatus: string;
  validationMessage?: string;
  uploadedAt?: string;
  uploadedBy?: UserRef;
}

export interface WorkflowState {
  submittedAt?: string;
  submittedBy?: UserRef;
  reviewerGroupId?: string;
  latestDecision?: "APPROVED" | "REJECTED" | "CHANGES_REQUESTED";
  latestDecisionAt?: string;
  latestDecisionBy?: UserRef;
  latestComments?: string;
}

export interface ContentSummary {
  id: string;
  contentType: ContentType;
  title: string;
  summary?: string;
  status: ContentStatus;
  category: CategoryRef;
  tags: string[];
  priority: Priority;
  requiresAcknowledgement: boolean;
  acknowledgementStatus: AcknowledgementStatus;
  publishedAt?: string;
  expiresAt?: string;
  updatedAt: string;
}

export interface ContentDetail extends ContentSummary {
  slug: string;
  body?: string;
  audience: AudienceRef;
  owner: UserRef;
  author?: UserRef;
  effectiveFrom?: string;
  revisionNumber: number;
  versionToken: string;
  attachments: AttachmentMetadata[];
  workflow?: WorkflowState;
}

export interface ContentListResponse {
  items: ContentSummary[];
  page: PageMeta;
}

export interface PortalHome {
  requiredNotices: ContentSummary[];
  pinnedContent: ContentSummary[];
  recentContent: ContentSummary[];
  categories: Category[];
}

export interface ContentCreateRequest {
  contentType: ContentType;
  title: string;
  summary?: string;
  body?: string;
  categoryId: string;
  audienceId: string;
  ownerUserId: string;
  tags?: string[];
  priority?: Priority;
  requiresAcknowledgement?: boolean;
  effectiveFrom?: string;
  expiresAt?: string;
}

export interface ContentUpdateRequest extends ContentCreateRequest {
  versionToken: string;
  changeSummary?: string;
}

export interface Tag {
  id: string;
  name: string;
  description?: string;
  active: boolean;
}

export interface Audience {
  id: string;
  name: string;
  audienceType: string;
  criteria: Record<string, unknown>;
  active: boolean;
}

export interface RoleAssignment {
  id: string;
  user: UserRef;
  role: RoleCode;
  scopeType: string;
  scopeId?: string;
  assignedAt: string;
  expiresAt?: string;
}

export interface AuditEvent {
  id: string;
  actor: UserRef;
  action: string;
  targetType: string;
  targetId: string;
  summary: string;
  details: Record<string, unknown>;
  occurredAt: string;
  requestId?: string;
}

export interface Acknowledgement {
  id: string;
  contentId: string;
  versionId: string;
  user: UserRef;
  acknowledgedAt: string;
}

export interface UserAcknowledgementItem {
  content: ContentSummary;
  status: "PENDING" | "COMPLETED";
  acknowledgedAt?: string;
  dueAt?: string;
}

export interface AcknowledgementReport {
  contentId: string;
  targetedCount: number;
  acknowledgedCount: number;
  pendingCount: number;
  completionRate: number;
  items: Array<{ user: UserRef; department?: string; status: "PENDING" | "COMPLETED"; acknowledgedAt?: string }>;
}

export function getPersona() {
  return localStorage.getItem("cms.persona") ?? DEFAULT_PERSONA;
}

export function setPersona(persona: string) {
  localStorage.setItem("cms.persona", persona);
  window.dispatchEvent(new CustomEvent("cms-persona-changed", { detail: persona }));
}

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      "X-CMS-User": getPersona(),
      ...(options.headers ?? {})
    }
  });

  if (!response.ok) {
    let payload: ApiErrorPayload = { code: "HTTP_ERROR", message: `Request failed with status ${response.status}` };
    try {
      payload = await response.json();
    } catch {
      payload.message = response.statusText || payload.message;
    }
    throw new ApiError(response.status, payload);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export function useApi<T>(loader: () => Promise<T>, deps: unknown[]) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | Error | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    loader()
      .then((value) => {
        if (!cancelled) {
          setData(value);
        }
      })
      .catch((reason: ApiError | Error) => {
        if (!cancelled) {
          setError(reason);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
    // The caller owns the dependency list so feature pages can refresh on route/query changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, error, loading, reload: () => loader().then(setData) };
}

export function formatDate(value?: string) {
  if (!value) {
    return "-";
  }
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

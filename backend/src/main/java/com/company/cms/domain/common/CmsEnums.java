package com.company.cms.domain.common;

public final class CmsEnums {
    private CmsEnums() {
    }

    public enum ContentType {
        KNOWLEDGE_ARTICLE,
        DOCUMENT_RECORD,
        NOTICE
    }

    public enum ContentStatus {
        DRAFT,
        IN_REVIEW,
        REJECTED,
        SCHEDULED,
        PUBLISHED,
        EXPIRED,
        ARCHIVED,
        DELETED
    }

    public enum Priority {
        NORMAL,
        PINNED,
        URGENT
    }

    public enum RoleCode {
        EMPLOYEE,
        AUTHOR,
        REVIEWER,
        PUBLISHER,
        NOTICE_MANAGER,
        EDITOR,
        AUDITOR,
        ADMINISTRATOR
    }

    public enum AudienceType {
        ALL_COMPANY,
        DEPARTMENT,
        ROLE,
        GROUP,
        CUSTOM
    }

    public enum UploadStatus {
        PENDING,
        UPLOADED,
        FAILED,
        REMOVED
    }

    public enum AttachmentValidationStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        SCAN_FAILED
    }

    public enum ReviewDecisionAction {
        APPROVE,
        REJECT,
        REQUEST_CHANGES
    }

    public enum ReviewDecisionState {
        APPROVED,
        REJECTED,
        CHANGES_REQUESTED
    }

    public enum AcknowledgementStatus {
        NOT_REQUIRED,
        PENDING,
        COMPLETED
    }
}

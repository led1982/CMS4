package com.company.cms.api.dto;

import com.company.cms.domain.common.CmsEnums.AcknowledgementStatus;
import com.company.cms.domain.common.CmsEnums.AttachmentValidationStatus;
import com.company.cms.domain.common.CmsEnums.AudienceType;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.common.CmsEnums.ContentType;
import com.company.cms.domain.common.CmsEnums.Priority;
import com.company.cms.domain.common.CmsEnums.ReviewDecisionAction;
import com.company.cms.domain.common.CmsEnums.ReviewDecisionState;
import com.company.cms.domain.common.CmsEnums.RoleCode;
import com.company.cms.domain.common.CmsEnums.UploadStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CmsDtos {
    private CmsDtos() {
    }

    public record FieldError(String field, String message) {
    }

    public record ErrorResponse(String code, String message, List<FieldError> fieldErrors, String requestId) {
    }

    public record PageMeta(int page, int size, long totalItems, int totalPages) {
    }

    public record UserRef(String id, String displayName, String email, String department) {
    }

    public record UserProfile(
        String id,
        String employeeId,
        String displayName,
        String email,
        String department,
        List<RoleCode> roles,
        List<String> capabilities
    ) {
    }

    public record CategoryRef(String id, String name) {
    }

    public record AudienceRef(String id, String name, AudienceType audienceType) {
    }

    public record PortalHome(
        List<ContentSummary> requiredNotices,
        List<ContentSummary> pinnedContent,
        List<ContentSummary> recentContent,
        List<Category> categories
    ) {
    }

    public record ContentListResponse(List<ContentSummary> items, PageMeta page) {
    }

    public record ContentSummary(
        String id,
        ContentType contentType,
        String title,
        String summary,
        ContentStatus status,
        CategoryRef category,
        List<String> tags,
        Priority priority,
        boolean requiresAcknowledgement,
        AcknowledgementStatus acknowledgementStatus,
        Instant publishedAt,
        Instant expiresAt,
        Instant updatedAt
    ) {
    }

    public record ContentDetail(
        String id,
        ContentType contentType,
        String title,
        String slug,
        String summary,
        String body,
        ContentStatus status,
        CategoryRef category,
        AudienceRef audience,
        UserRef owner,
        UserRef author,
        List<String> tags,
        Priority priority,
        boolean requiresAcknowledgement,
        AcknowledgementStatus acknowledgementStatus,
        Instant effectiveFrom,
        Instant expiresAt,
        Instant publishedAt,
        int revisionNumber,
        String versionToken,
        List<AttachmentMetadata> attachments,
        WorkflowState workflow,
        Instant updatedAt
    ) {
    }

    public record ContentCreateRequest(
        @NotNull ContentType contentType,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 500) String summary,
        String body,
        @NotBlank String categoryId,
        @NotBlank String audienceId,
        @NotBlank String ownerUserId,
        List<String> tags,
        Priority priority,
        Boolean requiresAcknowledgement,
        Instant effectiveFrom,
        Instant expiresAt
    ) {
    }

    public record ContentUpdateRequest(
        @NotNull ContentType contentType,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 500) String summary,
        String body,
        @NotBlank String categoryId,
        @NotBlank String audienceId,
        @NotBlank String ownerUserId,
        List<String> tags,
        Priority priority,
        Boolean requiresAcknowledgement,
        Instant effectiveFrom,
        Instant expiresAt,
        @NotBlank String versionToken,
        @Size(max = 500) String changeSummary
    ) {
    }

    public record SubmitReviewRequest(
        @NotBlank String versionToken,
        @NotBlank @Size(max = 500) String changeSummary,
        String requestedReviewerGroupId
    ) {
    }

    public record ReviewDecisionRequest(
        @NotNull ReviewDecisionAction decision,
        @Size(max = 2000) String comments,
        @NotBlank String versionToken
    ) {
    }

    public record PublishRequest(@NotBlank String versionToken, Instant publishAt) {
    }

    public record WorkflowState(
        Instant submittedAt,
        UserRef submittedBy,
        String reviewerGroupId,
        ReviewDecisionState latestDecision,
        Instant latestDecisionAt,
        UserRef latestDecisionBy,
        String latestComments
    ) {
    }

    public record ContentVersion(
        String id,
        String contentId,
        int versionNumber,
        ContentStatus status,
        String changeSummary,
        UserRef createdBy,
        Instant createdAt
    ) {
    }

    public record AttachmentCreateRequest(
        @NotBlank @Size(max = 255) String fileName,
        @NotBlank String mimeType,
        @Min(1) long sizeBytes,
        String checksum
    ) {
    }

    public record AttachmentMetadata(
        String id,
        String fileName,
        String mimeType,
        long sizeBytes,
        UploadStatus uploadStatus,
        AttachmentValidationStatus validationStatus,
        String validationMessage,
        Instant uploadedAt,
        UserRef uploadedBy
    ) {
    }

    public record AttachmentDownload(String attachmentId, String downloadUrl, Instant expiresAt) {
    }

    public record Acknowledgement(String id, String contentId, String versionId, UserRef user, Instant acknowledgedAt) {
    }

    public record Bookmark(String id, ContentSummary content, Instant savedAt) {
    }

    public record BookmarkCreateRequest(@NotBlank String contentId) {
    }

    public record UserAcknowledgementItem(ContentSummary content, String status, Instant acknowledgedAt, Instant dueAt) {
    }

    public record AcknowledgementReport(
        String contentId,
        int targetedCount,
        int acknowledgedCount,
        int pendingCount,
        double completionRate,
        List<AcknowledgementReportItem> items
    ) {
    }

    public record AcknowledgementReportItem(UserRef user, String department, String status, Instant acknowledgedAt) {
    }

    public record Category(
        String id,
        String parentId,
        String name,
        String slug,
        String description,
        String ownerUserId,
        String defaultAudienceId,
        String defaultReviewerGroupId,
        int sortOrder,
        boolean active,
        List<Category> children
    ) {
    }

    public record CategoryCreateRequest(
        String parentId,
        @NotBlank @Size(max = 120) String name,
        String description,
        @NotBlank String ownerUserId,
        String defaultAudienceId,
        String defaultReviewerGroupId,
        Integer sortOrder
    ) {
    }

    public record CategoryUpdateRequest(
        String parentId,
        @NotBlank @Size(max = 120) String name,
        String description,
        @NotBlank String ownerUserId,
        String defaultAudienceId,
        String defaultReviewerGroupId,
        Integer sortOrder,
        Boolean active
    ) {
    }

    public record Tag(String id, String name, String description, boolean active) {
    }

    public record TagCreateRequest(@NotBlank @Size(max = 60) String name, String description) {
    }

    public record Audience(String id, String name, AudienceType audienceType, Map<String, Object> criteria, boolean active) {
    }

    public record AudienceCreateRequest(
        @NotBlank String name,
        @NotNull AudienceType audienceType,
        @NotNull Map<String, Object> criteria
    ) {
    }

    public record RoleAssignment(
        String id,
        UserRef user,
        RoleCode role,
        String scopeType,
        String scopeId,
        Instant assignedAt,
        Instant expiresAt
    ) {
    }

    public record RoleAssignmentCreateRequest(
        @NotBlank String userId,
        @NotNull RoleCode role,
        @NotBlank String scopeType,
        String scopeId,
        Instant expiresAt
    ) {
    }

    public record AuditEventListResponse(List<AuditEvent> items, PageMeta page) {
    }

    public record ContentHealthReport(
        List<ContentSummary> stale,
        List<ContentSummary> expiring,
        List<ContentSummary> archived,
        List<ContentSummary> recentlyPublished
    ) {
    }

    public record AuditEvent(
        String id,
        UserRef actor,
        String action,
        String targetType,
        String targetId,
        String summary,
        Map<String, Object> details,
        Instant occurredAt,
        String requestId
    ) {
    }
}

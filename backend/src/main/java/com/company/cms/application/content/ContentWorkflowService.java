package com.company.cms.application.content;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.ContentCreateRequest;
import com.company.cms.api.dto.CmsDtos.ContentUpdateRequest;
import com.company.cms.api.dto.CmsDtos.PublishRequest;
import com.company.cms.api.dto.CmsDtos.SubmitReviewRequest;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.common.CmsEnums.AttachmentValidationStatus;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.content.ContentItem;
import com.company.cms.domain.notification.NotificationService;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContentWorkflowService {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ContentWorkflowService(
        ContentRepository contentRepository,
        AuthorizationPolicy authorizationPolicy,
        AuditService auditService,
        NotificationService notificationService
    ) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public ContentItem create(ContentCreateRequest request, CmsSecurityContext context) {
        if (!authorizationPolicy.canCreateContent(context)) {
            throw ApiException.forbidden("Only authors, editors, or administrators can create content");
        }
        validateDates(request.effectiveFrom(), request.expiresAt());
        var item = new ContentItem(
            request.contentType(),
            request.title(),
            request.summary(),
            request.body(),
            request.ownerUserId(),
            context.currentUser().id(),
            request.categoryId(),
            request.audienceId(),
            request.priority(),
            Boolean.TRUE.equals(request.requiresAcknowledgement()),
            request.effectiveFrom(),
            request.expiresAt(),
            tags(request.tags())
        );
        contentRepository.save(item);
        auditService.record(context.currentUser().id(), "CREATED", "CONTENT_ITEM", item.getId(), "Created draft content");
        return item;
    }

    public ContentItem update(String contentId, ContentUpdateRequest request, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canCreateContent(context) && !context.currentUser().id().equals(item.getAuthorUserId())) {
            throw ApiException.forbidden("Only the author, editor, or administrator can update content");
        }
        ensureToken(item, request.versionToken());
        if (item.getStatus() == ContentStatus.IN_REVIEW) {
            throw ApiException.conflict("Content in review cannot be edited until a decision is made");
        }
        validateDates(request.effectiveFrom(), request.expiresAt());
        item.applyDraft(
            request.contentType(),
            request.title(),
            request.summary(),
            request.body(),
            request.ownerUserId(),
            request.categoryId(),
            request.audienceId(),
            request.priority(),
            Boolean.TRUE.equals(request.requiresAcknowledgement()),
            request.effectiveFrom(),
            request.expiresAt(),
            tags(request.tags())
        );
        if (request.changeSummary() != null && !request.changeSummary().isBlank()) {
            item.createVersion(request.changeSummary(), context.currentUser().id());
        }
        contentRepository.save(item);
        auditService.record(context.currentUser().id(), "UPDATED", "CONTENT_ITEM", item.getId(), "Updated draft content");
        return item;
    }

    public ContentItem submitForReview(String contentId, SubmitReviewRequest request, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canCreateContent(context) && !context.currentUser().id().equals(item.getAuthorUserId())) {
            throw ApiException.forbidden("Only the author, editor, or administrator can submit content");
        }
        ensureToken(item, request.versionToken());
        if (item.getStatus() != ContentStatus.DRAFT && item.getStatus() != ContentStatus.REJECTED) {
            throw ApiException.conflict("Only draft or rejected content can be submitted for review");
        }
        if (isBlank(item.getTitle())
            || isBlank(item.getSummary())
            || isBlank(item.getBody())
            || isBlank(item.getOwnerUserId())
            || isBlank(item.getCategoryId())
            || isBlank(item.getAudienceId())) {
            throw ApiException.badRequest("MISSING_REQUIRED_METADATA", "Title, summary, body, owner, category, and audience are required before review");
        }
        boolean hasRejectedAttachment = contentRepository.listAttachments(item.getId()).stream()
            .anyMatch(attachment -> attachment.getValidationStatus() == AttachmentValidationStatus.REJECTED
                || attachment.getValidationStatus() == AttachmentValidationStatus.SCAN_FAILED);
        if (hasRejectedAttachment) {
            throw ApiException.badRequest("ATTACHMENT_BLOCKED", "Rejected attachments must be removed before review");
        }
        item.submitForReview(request.requestedReviewerGroupId(), context.currentUser().id(), request.changeSummary());
        contentRepository.save(item);
        auditService.record(context.currentUser().id(), "SUBMITTED", "CONTENT_ITEM", item.getId(), "Submitted content for review");
        notificationService.notify("reviewer", "REVIEW_REQUESTED", item.getId(), "Content is ready for review: " + item.getTitle());
        return item;
    }

    public ContentItem publish(String contentId, PublishRequest request, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canPublish(context)) {
            throw ApiException.forbidden("Only reviewers, editors, or administrators can publish content");
        }
        ensureToken(item, request.versionToken());
        if (item.getWorkflow().getLatestDecision() == null || !item.getWorkflow().getLatestDecision().name().equals("APPROVED")) {
            throw ApiException.conflict("Only approved content can be published");
        }
        item.publish(request.publishAt());
        contentRepository.save(item);
        auditService.record(context.currentUser().id(), item.getStatus() == ContentStatus.PUBLISHED ? "PUBLISHED" : "SCHEDULED", "CONTENT_ITEM", item.getId(), "Published or scheduled content");
        notificationService.notify(item.getAuthorUserId(), "PUBLISHED", item.getId(), "Content was published: " + item.getTitle());
        return item;
    }

    public ContentItem archive(String contentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canPublish(context)) {
            throw ApiException.forbidden("Only reviewers, editors, or administrators can archive content");
        }
        if (item.getStatus() != ContentStatus.PUBLISHED && item.getStatus() != ContentStatus.EXPIRED) {
            throw ApiException.conflict("Only published or expired content can be archived");
        }
        item.archive();
        contentRepository.save(item);
        auditService.record(context.currentUser().id(), "ARCHIVED", "CONTENT_ITEM", item.getId(), "Archived content");
        return item;
    }

    public void delete(String contentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canCreateContent(context) && !authorizationPolicy.canAdminister(context)) {
            throw ApiException.forbidden("Only authorized content managers can delete content");
        }
        if (item.getStatus() == ContentStatus.PUBLISHED && !authorizationPolicy.canAdminister(context)) {
            throw ApiException.conflict("Published content must be archived before deletion");
        }
        item.delete();
        contentRepository.save(item);
        auditService.record(context.currentUser().id(), "DELETED", "CONTENT_ITEM", item.getId(), "Deleted content");
    }

    public ContentItem getForUser(String contentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canViewContent(context, item)) {
            auditService.record(context.currentUser().id(), "DENIED", "CONTENT_ITEM", contentId, "Denied content detail access");
            throw ApiException.forbidden("You do not have access to this content");
        }
        if (authorizationPolicy.canViewPortalContent(context, item)) {
            item.recordView();
            contentRepository.save(item);
            auditService.record(context.currentUser().id(), "VIEWED", "CONTENT_ITEM", item.getId(), "Viewed portal content");
        }
        return item;
    }

    public List<ContentItem.ContentVersion> versions(String contentId, CmsSecurityContext context) {
        var item = getForUser(contentId, context);
        return item.getVersions();
    }

    public void ensureToken(ContentItem item, String versionToken) {
        if (!item.getVersionToken().equals(versionToken)) {
            throw ApiException.conflict("Content has changed; refresh before retrying");
        }
    }

    private void validateDates(Instant effectiveFrom, Instant expiresAt) {
        if (effectiveFrom != null && expiresAt != null && !expiresAt.isAfter(effectiveFrom)) {
            throw ApiException.badRequest("INVALID_DATE_RANGE", "Expiry date must be later than the effective date");
        }
    }

    private List<String> tags(List<String> tags) {
        return tags == null ? List.of() : tags;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

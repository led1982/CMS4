package com.company.cms.application.review;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.ReviewDecisionRequest;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.common.CmsEnums.ReviewDecisionAction;
import com.company.cms.domain.common.CmsEnums.ReviewDecisionState;
import com.company.cms.domain.content.ContentItem;
import com.company.cms.domain.notification.NotificationService;
import com.company.cms.domain.review.ReviewDecision;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ReviewService(ContentRepository contentRepository, AuthorizationPolicy authorizationPolicy, AuditService auditService, NotificationService notificationService) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public ContentItem review(String contentId, ReviewDecisionRequest request, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canReview(context)) {
            throw ApiException.forbidden("Only reviewers, editors, or administrators can review content");
        }
        if (!item.getVersionToken().equals(request.versionToken())) {
            throw ApiException.conflict("Content has changed; refresh before retrying");
        }
        if (item.getStatus() != ContentStatus.IN_REVIEW) {
            throw ApiException.conflict("Only in-review content can receive a review decision");
        }
        if ((request.decision() == ReviewDecisionAction.REJECT || request.decision() == ReviewDecisionAction.REQUEST_CHANGES)
            && (request.comments() == null || request.comments().isBlank())) {
            throw ApiException.badRequest("COMMENTS_REQUIRED", "Reviewer comments are required for rejection or change requests");
        }

        ReviewDecisionState state = switch (request.decision()) {
            case APPROVE -> ReviewDecisionState.APPROVED;
            case REJECT -> ReviewDecisionState.REJECTED;
            case REQUEST_CHANGES -> ReviewDecisionState.CHANGES_REQUESTED;
        };
        item.markReviewDecision(state, context.currentUser().id(), request.comments());
        contentRepository.save(item);
        contentRepository.saveReviewDecision(ReviewDecision.create(contentId, latestVersionId(item), context.currentUser().id(), state, request.comments()));
        auditService.record(context.currentUser().id(), state.name(), "CONTENT_ITEM", item.getId(), "Recorded review decision");
        notificationService.notify(item.getAuthorUserId(), state.name(), item.getId(), "Review decision for: " + item.getTitle());
        return item;
    }

    public List<ContentItem> queue(CmsSecurityContext context) {
        if (!authorizationPolicy.canReview(context)) {
            throw ApiException.forbidden("Only reviewers, editors, or administrators can view the review queue");
        }
        return contentRepository.reviewQueue();
    }

    private String latestVersionId(ContentItem item) {
        var versions = item.getVersions();
        return versions.isEmpty() ? null : versions.get(versions.size() - 1).id();
    }
}

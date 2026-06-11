package com.company.cms.infrastructure.persistence;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.domain.acknowledgement.Acknowledgement;
import com.company.cms.domain.common.CmsEnums.AttachmentValidationStatus;
import com.company.cms.domain.common.CmsEnums.ContentType;
import com.company.cms.domain.common.CmsEnums.Priority;
import com.company.cms.domain.common.CmsEnums.ReviewDecisionState;
import com.company.cms.domain.content.Attachment;
import com.company.cms.domain.content.ContentItem;
import com.company.cms.domain.review.ReviewDecision;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ContentRepository {
    private final Map<String, ContentItem> contentItems = new ConcurrentHashMap<>();
    private final Map<String, Attachment> attachments = new ConcurrentHashMap<>();
    private final Map<String, ReviewDecision> reviewDecisions = new ConcurrentHashMap<>();
    private final Map<String, Acknowledgement> acknowledgements = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        var notice = new ContentItem(
            ContentType.NOTICE,
            "Annual Security Awareness Notice",
            "Required annual security awareness acknowledgement for all employees.",
            "## Annual Security Awareness\n\nReview the security practices for passwords, phishing, and approved data handling. This notice requires acknowledgement.",
            "author",
            "author",
            GovernanceRepository.HR_POLICIES_CATEGORY_ID,
            GovernanceRepository.ALL_EMPLOYEES_AUDIENCE_ID,
            Priority.URGENT,
            true,
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().plus(30, ChronoUnit.DAYS),
            List.of("Policy", "Security")
        );
        notice.submitForReview(GovernanceRepository.DEFAULT_REVIEWER_GROUP_ID, "author", "Initial required notice");
        notice.markReviewDecision(ReviewDecisionState.APPROVED, "reviewer", "Approved for annual rollout");
        notice.publish(null);
        save(notice);
        saveAttachment(new Attachment(notice.getId(), "security-awareness.pdf", "application/pdf", 524_288, "seed", "author", AttachmentValidationStatus.ACCEPTED, null));

        var guide = new ContentItem(
            ContentType.KNOWLEDGE_ARTICLE,
            "Password Reset Guide",
            "How to reset your password and recover account access.",
            "Use the identity portal to reset passwords. Contact IT support when MFA is unavailable.",
            "admin",
            "admin",
            GovernanceRepository.IT_GUIDES_CATEGORY_ID,
            GovernanceRepository.ALL_EMPLOYEES_AUDIENCE_ID,
            Priority.PINNED,
            false,
            Instant.now().minus(5, ChronoUnit.DAYS),
            null,
            List.of("Security")
        );
        guide.submitForReview(GovernanceRepository.DEFAULT_REVIEWER_GROUP_ID, "admin", "Seed guide");
        guide.markReviewDecision(ReviewDecisionState.APPROVED, "reviewer", "Approved");
        guide.publish(null);
        save(guide);
    }

    public ContentItem save(ContentItem item) {
        contentItems.put(item.getId(), item);
        return item;
    }

    public ContentItem get(String id) {
        var item = contentItems.get(id);
        if (item == null) {
            throw ApiException.notFound("Content was not found or is unavailable");
        }
        return item;
    }

    public List<ContentItem> list() {
        return contentItems.values().stream()
            .sorted(Comparator.comparing(ContentItem::getUpdatedAt).reversed())
            .toList();
    }

    public Attachment saveAttachment(Attachment attachment) {
        attachments.put(attachment.getId(), attachment);
        return attachment;
    }

    public List<Attachment> listAttachments(String contentItemId) {
        return attachments.values().stream()
            .filter(attachment -> attachment.getContentItemId().equals(contentItemId))
            .sorted(Comparator.comparing(Attachment::getUploadedAt))
            .toList();
    }

    public Attachment getAttachment(String contentItemId, String attachmentId) {
        var attachment = attachments.get(attachmentId);
        if (attachment == null || !attachment.getContentItemId().equals(contentItemId)) {
            throw ApiException.notFound("Attachment was not found");
        }
        return attachment;
    }

    public Attachment getAttachment(String attachmentId) {
        var attachment = attachments.get(attachmentId);
        if (attachment == null) {
            throw ApiException.notFound("Attachment was not found");
        }
        return attachment;
    }

    public ReviewDecision saveReviewDecision(ReviewDecision decision) {
        reviewDecisions.put(decision.id(), decision);
        return decision;
    }

    public List<ReviewDecision> listReviewDecisions(String contentItemId) {
        return reviewDecisions.values().stream()
            .filter(decision -> decision.contentItemId().equals(contentItemId))
            .sorted(Comparator.comparing(ReviewDecision::decidedAt).reversed())
            .toList();
    }

    public Acknowledgement saveAcknowledgement(Acknowledgement acknowledgement) {
        String key = acknowledgement.contentItemId() + ":" + acknowledgement.versionId() + ":" + acknowledgement.userId();
        if (acknowledgements.containsKey(key)) {
            throw ApiException.conflict("This notice version has already been acknowledged");
        }
        acknowledgements.put(key, acknowledgement);
        return acknowledgement;
    }

    public Optional<Acknowledgement> findAcknowledgement(String contentItemId, String versionId, String userId) {
        return Optional.ofNullable(acknowledgements.get(contentItemId + ":" + versionId + ":" + userId));
    }

    public List<Acknowledgement> listAcknowledgements(String contentItemId) {
        return acknowledgements.values().stream()
            .filter(acknowledgement -> acknowledgement.contentItemId().equals(contentItemId))
            .sorted(Comparator.comparing(Acknowledgement::acknowledgedAt).reversed())
            .toList();
    }

    public List<ContentItem> reviewQueue() {
        return contentItems.values().stream()
            .filter(item -> item.getStatus().name().equals("IN_REVIEW"))
            .sorted(Comparator.comparing(ContentItem::getUpdatedAt))
            .toList();
    }

    public List<ContentItem> authorItems(String authorUserId) {
        return contentItems.values().stream()
            .filter(item -> item.getAuthorUserId().equals(authorUserId) || item.getOwnerUserId().equals(authorUserId))
            .sorted(Comparator.comparing(ContentItem::getUpdatedAt).reversed())
            .toList();
    }

    public List<Attachment> attachmentsFor(ContentItem item) {
        return new ArrayList<>(listAttachments(item.getId()));
    }
}

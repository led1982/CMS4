package com.company.cms.domain.content;

import com.company.cms.domain.common.CmsEnums.AcknowledgementStatus;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.common.CmsEnums.ContentType;
import com.company.cms.domain.common.CmsEnums.Priority;
import com.company.cms.domain.common.CmsEnums.ReviewDecisionState;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ContentItem {
    private final String id;
    private ContentType contentType;
    private String title;
    private String slug;
    private String summary;
    private String body;
    private ContentStatus status;
    private String ownerUserId;
    private String authorUserId;
    private String categoryId;
    private String audienceId;
    private Priority priority;
    private boolean requiresAcknowledgement;
    private Instant effectiveFrom;
    private Instant expiresAt;
    private Instant publishedAt;
    private Instant archivedAt;
    private String currentPublishedVersionId;
    private String draftVersionId;
    private int revisionNumber;
    private long viewCount;
    private final Instant createdAt;
    private Instant updatedAt;
    private String versionToken;
    private final List<String> tags = new ArrayList<>();
    private final List<ContentVersion> versions = new ArrayList<>();
    private final Workflow workflow = new Workflow();

    public ContentItem(
        ContentType contentType,
        String title,
        String summary,
        String body,
        String ownerUserId,
        String authorUserId,
        String categoryId,
        String audienceId,
        Priority priority,
        boolean requiresAcknowledgement,
        Instant effectiveFrom,
        Instant expiresAt,
        List<String> tags
    ) {
        this.id = UUID.randomUUID().toString();
        this.contentType = Objects.requireNonNull(contentType);
        this.title = Objects.requireNonNull(title);
        this.slug = slugify(title);
        this.summary = summary;
        this.body = body;
        this.status = ContentStatus.DRAFT;
        this.ownerUserId = Objects.requireNonNull(ownerUserId);
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.categoryId = Objects.requireNonNull(categoryId);
        this.audienceId = Objects.requireNonNull(audienceId);
        this.priority = priority == null ? Priority.NORMAL : priority;
        this.requiresAcknowledgement = requiresAcknowledgement;
        this.effectiveFrom = effectiveFrom;
        this.expiresAt = expiresAt;
        this.revisionNumber = 1;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.versionToken = UUID.randomUUID().toString();
        replaceTags(tags);
    }

    public void applyDraft(
        ContentType contentType,
        String title,
        String summary,
        String body,
        String ownerUserId,
        String categoryId,
        String audienceId,
        Priority priority,
        boolean requiresAcknowledgement,
        Instant effectiveFrom,
        Instant expiresAt,
        List<String> tags
    ) {
        this.contentType = contentType;
        this.title = title;
        this.slug = slugify(title);
        this.summary = summary;
        this.body = body;
        this.ownerUserId = ownerUserId;
        this.categoryId = categoryId;
        this.audienceId = audienceId;
        this.priority = priority == null ? Priority.NORMAL : priority;
        this.requiresAcknowledgement = requiresAcknowledgement;
        this.effectiveFrom = effectiveFrom;
        this.expiresAt = expiresAt;
        if (this.status == ContentStatus.PUBLISHED) {
            this.status = ContentStatus.DRAFT;
            this.revisionNumber++;
        }
        replaceTags(tags);
        touch();
    }

    public ContentVersion createVersion(String changeSummary, String createdBy) {
        var version = new ContentVersion(
            UUID.randomUUID().toString(),
            id,
            versions.size() + 1,
            status,
            title,
            summary,
            body,
            changeSummary,
            createdBy,
            Instant.now()
        );
        versions.add(version);
        draftVersionId = version.id();
        touch();
        return version;
    }

    public void submitForReview(String reviewerGroupId, String submittedBy, String changeSummary) {
        createVersion(changeSummary, submittedBy);
        status = ContentStatus.IN_REVIEW;
        workflow.submittedAt = Instant.now();
        workflow.submittedBy = submittedBy;
        workflow.reviewerGroupId = reviewerGroupId;
        workflow.latestDecision = null;
        workflow.latestComments = null;
        touch();
    }

    public void markReviewDecision(ReviewDecisionState decision, String reviewerUserId, String comments) {
        workflow.latestDecision = decision;
        workflow.latestDecisionAt = Instant.now();
        workflow.latestDecisionBy = reviewerUserId;
        workflow.latestComments = comments;
        if (decision == ReviewDecisionState.APPROVED) {
            status = ContentStatus.SCHEDULED;
        } else {
            status = ContentStatus.REJECTED;
        }
        touch();
    }

    public void publish(Instant publishAt) {
        Instant now = Instant.now();
        if (publishAt != null && publishAt.isAfter(now)) {
            status = ContentStatus.SCHEDULED;
            effectiveFrom = publishAt;
        } else {
            status = ContentStatus.PUBLISHED;
            publishedAt = now;
            currentPublishedVersionId = draftVersionId;
        }
        touch();
    }

    public void archive() {
        status = ContentStatus.ARCHIVED;
        archivedAt = Instant.now();
        touch();
    }

    public void delete() {
        status = ContentStatus.DELETED;
        touch();
    }

    public boolean isPortalVisibleNow() {
        Instant now = Instant.now();
        if (status != ContentStatus.PUBLISHED) {
            return false;
        }
        if (effectiveFrom != null && effectiveFrom.isAfter(now)) {
            return false;
        }
        return expiresAt == null || expiresAt.isAfter(now);
    }

    public AcknowledgementStatus acknowledgementStatus(boolean acknowledged) {
        if (!requiresAcknowledgement) {
            return AcknowledgementStatus.NOT_REQUIRED;
        }
        return acknowledged ? AcknowledgementStatus.COMPLETED : AcknowledgementStatus.PENDING;
    }

    public void touch() {
        updatedAt = Instant.now();
        versionToken = UUID.randomUUID().toString();
    }

    private void replaceTags(List<String> newTags) {
        tags.clear();
        if (newTags == null) {
            return;
        }
        newTags.stream()
            .filter(tag -> tag != null && !tag.isBlank())
            .map(String::trim)
            .distinct()
            .forEach(tags::add);
    }

    public static String slugify(String value) {
        String slug = value == null ? "content" : value.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9가-힣]+", "-")
            .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "content" : slug;
    }

    public String getId() {
        return id;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getAudienceId() {
        return audienceId;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isRequiresAcknowledgement() {
        return requiresAcknowledgement;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getCurrentPublishedVersionId() {
        return currentPublishedVersionId;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public long getViewCount() {
        return viewCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getVersionToken() {
        return versionToken;
    }

    public List<String> getTags() {
        return List.copyOf(tags);
    }

    public List<ContentVersion> getVersions() {
        return List.copyOf(versions);
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public record ContentVersion(
        String id,
        String contentId,
        int versionNumber,
        ContentStatus status,
        String titleSnapshot,
        String summarySnapshot,
        String bodySnapshot,
        String changeSummary,
        String createdBy,
        Instant createdAt
    ) {
    }

    public static class Workflow {
        private Instant submittedAt;
        private String submittedBy;
        private String reviewerGroupId;
        private ReviewDecisionState latestDecision;
        private Instant latestDecisionAt;
        private String latestDecisionBy;
        private String latestComments;

        public Instant getSubmittedAt() {
            return submittedAt;
        }

        public String getSubmittedBy() {
            return submittedBy;
        }

        public String getReviewerGroupId() {
            return reviewerGroupId;
        }

        public ReviewDecisionState getLatestDecision() {
            return latestDecision;
        }

        public Instant getLatestDecisionAt() {
            return latestDecisionAt;
        }

        public String getLatestDecisionBy() {
            return latestDecisionBy;
        }

        public String getLatestComments() {
            return latestComments;
        }
    }
}

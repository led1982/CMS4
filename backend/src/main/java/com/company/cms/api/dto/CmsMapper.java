package com.company.cms.api.dto;

import com.company.cms.api.dto.CmsDtos.AttachmentDownload;
import com.company.cms.api.dto.CmsDtos.AttachmentMetadata;
import com.company.cms.api.dto.CmsDtos.AudienceRef;
import com.company.cms.api.dto.CmsDtos.CategoryRef;
import com.company.cms.api.dto.CmsDtos.ContentDetail;
import com.company.cms.api.dto.CmsDtos.ContentSummary;
import com.company.cms.api.dto.CmsDtos.ContentVersion;
import com.company.cms.api.dto.CmsDtos.WorkflowState;
import com.company.cms.domain.acknowledgement.Acknowledgement;
import com.company.cms.domain.content.Attachment;
import com.company.cms.domain.content.ContentItem;
import com.company.cms.domain.governance.Category;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.infrastructure.persistence.GovernanceRepository;
import com.company.cms.security.CmsSecurityContext.Personas;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CmsMapper {
    private final GovernanceRepository governanceRepository;
    private final ContentRepository contentRepository;

    public CmsMapper(GovernanceRepository governanceRepository, ContentRepository contentRepository) {
        this.governanceRepository = governanceRepository;
        this.contentRepository = contentRepository;
    }

    public ContentSummary toSummary(ContentItem item, String userId) {
        var category = governanceRepository.getCategory(item.getCategoryId());
        boolean acknowledged = isAcknowledged(item, userId);
        return new ContentSummary(
            item.getId(),
            item.getContentType(),
            item.getTitle(),
            item.getSummary(),
            item.getStatus(),
            new CategoryRef(category.id(), category.name()),
            item.getTags(),
            item.getPriority(),
            item.isRequiresAcknowledgement(),
            item.acknowledgementStatus(acknowledged),
            item.getPublishedAt(),
            item.getExpiresAt(),
            item.getUpdatedAt()
        );
    }

    public ContentDetail toDetail(ContentItem item, String userId) {
        var category = governanceRepository.getCategory(item.getCategoryId());
        var audience = governanceRepository.getAudience(item.getAudienceId());
        return new ContentDetail(
            item.getId(),
            item.getContentType(),
            item.getTitle(),
            item.getSlug(),
            item.getSummary(),
            item.getBody(),
            item.getStatus(),
            new CategoryRef(category.id(), category.name()),
            new AudienceRef(audience.id(), audience.name(), audience.audienceType()),
            Personas.ref(item.getOwnerUserId()),
            Personas.ref(item.getAuthorUserId()),
            item.getTags(),
            item.getPriority(),
            item.isRequiresAcknowledgement(),
            item.acknowledgementStatus(isAcknowledged(item, userId)),
            item.getEffectiveFrom(),
            item.getExpiresAt(),
            item.getPublishedAt(),
            item.getRevisionNumber(),
            item.getVersionToken(),
            contentRepository.listAttachments(item.getId()).stream().map(this::toAttachment).toList(),
            toWorkflow(item),
            item.getUpdatedAt()
        );
    }

    public AttachmentMetadata toAttachment(Attachment attachment) {
        return new AttachmentMetadata(
            attachment.getId(),
            attachment.getFileName(),
            attachment.getMimeType(),
            attachment.getSizeBytes(),
            attachment.getUploadStatus(),
            attachment.getValidationStatus(),
            attachment.getValidationMessage(),
            attachment.getUploadedAt(),
            Personas.ref(attachment.getUploadedBy())
        );
    }

    public AttachmentDownload toDownload(Attachment attachment) {
        return new AttachmentDownload(
            attachment.getId(),
            "/api/v1/downloads/" + attachment.getId(),
            Instant.now().plusSeconds(300)
        );
    }

    public ContentVersion toVersion(ContentItem.ContentVersion version) {
        return new ContentVersion(
            version.id(),
            version.contentId(),
            version.versionNumber(),
            version.status(),
            version.changeSummary(),
            Personas.ref(version.createdBy()),
            version.createdAt()
        );
    }

    public CmsDtos.Category toCategory(Category category, List<CmsDtos.Category> children) {
        return new CmsDtos.Category(
            category.id(),
            category.parentId(),
            category.name(),
            category.slug(),
            category.description(),
            category.ownerUserId(),
            category.defaultAudienceId(),
            category.defaultReviewerGroupId(),
            category.sortOrder(),
            category.active(),
            children
        );
    }

    public CmsDtos.Tag toTag(Category.Tag tag) {
        return new CmsDtos.Tag(tag.id(), tag.name(), tag.description(), tag.active());
    }

    public CmsDtos.Audience toAudience(Category.Audience audience) {
        return new CmsDtos.Audience(audience.id(), audience.name(), audience.audienceType(), audience.criteria(), audience.active());
    }

    public CmsDtos.RoleAssignment toRoleAssignment(Category.RoleAssignment assignment) {
        return new CmsDtos.RoleAssignment(
            assignment.id(),
            Personas.ref(assignment.userId()),
            assignment.role(),
            assignment.scopeType(),
            assignment.scopeId(),
            assignment.assignedAt(),
            assignment.expiresAt()
        );
    }

    public CmsDtos.Acknowledgement toAcknowledgement(Acknowledgement acknowledgement) {
        return new CmsDtos.Acknowledgement(
            acknowledgement.id(),
            acknowledgement.contentItemId(),
            acknowledgement.versionId(),
            Personas.ref(acknowledgement.userId()),
            acknowledgement.acknowledgedAt()
        );
    }

    private WorkflowState toWorkflow(ContentItem item) {
        var workflow = item.getWorkflow();
        return new WorkflowState(
            workflow.getSubmittedAt(),
            workflow.getSubmittedBy() == null ? null : Personas.ref(workflow.getSubmittedBy()),
            workflow.getReviewerGroupId(),
            workflow.getLatestDecision(),
            workflow.getLatestDecisionAt(),
            workflow.getLatestDecisionBy() == null ? null : Personas.ref(workflow.getLatestDecisionBy()),
            workflow.getLatestComments()
        );
    }

    private boolean isAcknowledged(ContentItem item, String userId) {
        if (item.getCurrentPublishedVersionId() == null || userId == null) {
            return false;
        }
        return contentRepository.findAcknowledgement(item.getId(), item.getCurrentPublishedVersionId(), userId).isPresent();
    }
}

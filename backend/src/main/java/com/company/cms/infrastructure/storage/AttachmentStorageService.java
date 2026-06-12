package com.company.cms.infrastructure.storage;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.AttachmentCreateRequest;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.common.CmsEnums.AttachmentValidationStatus;
import com.company.cms.domain.content.Attachment;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AttachmentStorageService implements AttachmentStorage {
    private final long maxSingleFileBytes;
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;

    public AttachmentStorageService(
        @Value("${cms.attachments.max-single-file-bytes}") long maxSingleFileBytes,
        ContentRepository contentRepository,
        AuthorizationPolicy authorizationPolicy,
        AuditService auditService
    ) {
        this.maxSingleFileBytes = maxSingleFileBytes;
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
    }

    @Override
    public Attachment create(String contentId, AttachmentCreateRequest request, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canCreateContent(context) && !context.currentUser().id().equals(item.getAuthorUserId())) {
            throw ApiException.forbidden("Only content managers can attach files");
        }
        if (request.sizeBytes() > maxSingleFileBytes) {
            throw ApiException.payloadTooLarge("Single attachments are limited to 10MB");
        }
        boolean executable = request.fileName().toLowerCase().endsWith(".exe") || request.mimeType().equalsIgnoreCase("application/x-msdownload");
        var attachment = new Attachment(
            contentId,
            request.fileName(),
            request.mimeType(),
            request.sizeBytes(),
            request.checksum(),
            context.currentUser().id(),
            executable ? AttachmentValidationStatus.REJECTED : AttachmentValidationStatus.ACCEPTED,
            executable ? "Executable files are not allowed" : null
        );
        contentRepository.saveAttachment(attachment);
        auditService.record(context.currentUser().id(), "ATTACHMENT_UPLOADED", "ATTACHMENT", attachment.getId(), "Registered attachment metadata");
        return attachment;
    }

    @Override
    public List<Attachment> list(String contentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canViewContent(context, item)) {
            auditService.record(context.currentUser().id(), "DENIED", "CONTENT_ITEM", contentId, "Denied attachment metadata access");
            throw ApiException.forbidden("You do not have access to these attachments");
        }
        return contentRepository.listAttachments(contentId);
    }

    @Override
    public Attachment download(String contentId, String attachmentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        var attachment = contentRepository.getAttachment(contentId, attachmentId);
        if (!authorizationPolicy.canDownloadAttachment(context, item, attachment)) {
            auditService.record(context.currentUser().id(), "DENIED", "ATTACHMENT", attachment.getId(), "Denied attachment download");
            throw ApiException.forbidden("Attachment download is not allowed");
        }
        auditService.record(context.currentUser().id(), "DOWNLOADED", "ATTACHMENT", attachment.getId(), "Authorized attachment download");
        return attachment;
    }

    @Override
    public void delete(String contentId, String attachmentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        var attachment = contentRepository.getAttachment(contentId, attachmentId);
        if (!authorizationPolicy.canCreateContent(context) && !context.currentUser().id().equals(item.getAuthorUserId())) {
            throw ApiException.forbidden("Only content managers can delete attachments");
        }
        contentRepository.deleteAttachment(contentId, attachmentId);
        auditService.record(context.currentUser().id(), "ATTACHMENT_DELETED", "ATTACHMENT", attachment.getId(), "Deleted attachment metadata");
    }
}

package com.company.cms.api;

import com.company.cms.domain.audit.AuditService;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/downloads")
public class DownloadController {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;

    public DownloadController(ContentRepository contentRepository, AuthorizationPolicy authorizationPolicy, AuditService auditService) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<byte[]> download(@PathVariable String attachmentId, CmsSecurityContext context) {
        var attachment = contentRepository.getAttachment(attachmentId);
        var item = contentRepository.get(attachment.getContentItemId());
        if (!authorizationPolicy.canDownloadAttachment(context, item, attachment)) {
            auditService.record(context.currentUser().id(), "DENIED", "ATTACHMENT", attachment.getId(), "Denied direct attachment download");
            throw ApiExceptionHandler.ApiException.forbidden("Attachment download is not allowed");
        }
        auditService.record(context.currentUser().id(), "DOWNLOADED", "ATTACHMENT", attachment.getId(), "Downloaded attachment stream");
        byte[] body = ("Preview attachment placeholder for " + attachment.getFileName() + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(attachment.getFileName()).build().toString())
            .body(body);
    }
}

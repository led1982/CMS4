package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.AttachmentCreateRequest;
import com.company.cms.api.dto.CmsDtos.AttachmentDownload;
import com.company.cms.api.dto.CmsDtos.AttachmentMetadata;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.infrastructure.storage.AttachmentStorageService;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/content/{contentId}/attachments")
public class AttachmentController {
    private final AttachmentStorageService attachmentStorageService;
    private final CmsMapper mapper;

    public AttachmentController(AttachmentStorageService attachmentStorageService, CmsMapper mapper) {
        this.attachmentStorageService = attachmentStorageService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AttachmentMetadata> list(@PathVariable String contentId, CmsSecurityContext context) {
        return attachmentStorageService.list(contentId, context).stream().map(mapper::toAttachment).toList();
    }

    @PostMapping
    public ResponseEntity<AttachmentMetadata> create(@PathVariable String contentId, @Valid @RequestBody AttachmentCreateRequest request, CmsSecurityContext context) {
        var attachment = attachmentStorageService.create(contentId, request, context);
        return ResponseEntity.created(URI.create("/api/v1/content/" + contentId + "/attachments/" + attachment.getId()))
            .body(mapper.toAttachment(attachment));
    }

    @GetMapping("/{attachmentId}/download")
    public AttachmentDownload download(@PathVariable String contentId, @PathVariable String attachmentId, CmsSecurityContext context) {
        return mapper.toDownload(attachmentStorageService.download(contentId, attachmentId, context));
    }
}

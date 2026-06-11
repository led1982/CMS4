package com.company.cms.api;

import com.company.cms.infrastructure.persistence.ContentRepository;
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

    public DownloadController(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<byte[]> download(@PathVariable String attachmentId) {
        var attachment = contentRepository.getAttachment(attachmentId);
        byte[] body = ("Preview attachment placeholder for " + attachment.getFileName() + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(attachment.getFileName()).build().toString())
            .body(body);
    }
}

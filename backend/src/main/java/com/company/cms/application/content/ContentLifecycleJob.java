package com.company.cms.application.content;

import com.company.cms.domain.audit.AuditService;
import com.company.cms.infrastructure.persistence.ContentRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContentLifecycleJob {
    private final ContentRepository contentRepository;
    private final AuditService auditService;

    public ContentLifecycleJob(ContentRepository contentRepository, AuditService auditService) {
        this.contentRepository = contentRepository;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "${cms.lifecycle.scan-delay-ms:60000}")
    public void applyScheduledTransitions() {
        Instant now = Instant.now();
        contentRepository.list().forEach(item -> {
            if (item.publishIfDue(now)) {
                contentRepository.save(item);
                auditService.record("system", "PUBLISHED", "CONTENT_ITEM", item.getId(), "Published scheduled content");
            } else if (item.expireIfDue(now)) {
                contentRepository.save(item);
                auditService.record("system", "EXPIRED", "CONTENT_ITEM", item.getId(), "Expired portal content");
            }
        });
    }
}

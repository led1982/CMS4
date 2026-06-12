package com.company.cms.application.reporting;

import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.infrastructure.persistence.ContentRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContentHealthJob {
    private final ContentRepository contentRepository;
    private final AuditService auditService;

    public ContentHealthJob(ContentRepository contentRepository, AuditService auditService) {
        this.contentRepository = contentRepository;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "${cms.reporting.health-scan-delay-ms:3600000}")
    public void recordStaleContentSummary() {
        Instant staleBefore = Instant.now().minus(180, ChronoUnit.DAYS);
        long staleCount = contentRepository.list().stream()
            .filter(item -> item.getStatus() == ContentStatus.PUBLISHED)
            .filter(item -> item.getUpdatedAt().isBefore(staleBefore))
            .count();
        if (staleCount > 0) {
            auditService.record("system", "CONTENT_HEALTH_STALE", "REPORT", "content-health", "Detected stale published content", java.util.Map.of("staleCount", staleCount));
        }
    }
}

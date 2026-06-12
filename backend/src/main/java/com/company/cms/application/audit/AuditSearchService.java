package com.company.cms.application.audit;

import com.company.cms.api.dto.CmsDtos.AuditEventListResponse;
import com.company.cms.domain.audit.AuditService;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditSearchService {
    private final AuditService auditService;

    public AuditSearchService(AuditService auditService) {
        this.auditService = auditService;
    }

    public AuditEventListResponse search(String actorUserId, String targetType, String targetId, String action, int page, int size) {
        return auditService.search(actorUserId, targetType, targetId, action, page, size);
    }

    public AuditEventListResponse search(String actorUserId, String targetType, String targetId, String action, Instant from, Instant to, int page, int size) {
        return auditService.search(actorUserId, targetType, targetId, action, from, to, page, size);
    }
}

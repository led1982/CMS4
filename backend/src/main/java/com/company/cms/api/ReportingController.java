package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.AuditEventListResponse;
import com.company.cms.api.dto.CmsDtos.ContentHealthReport;
import com.company.cms.application.reporting.ContentHealthReportService;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class ReportingController {
    private final ContentHealthReportService contentHealthReportService;
    private final AuditService auditService;
    private final AuthorizationPolicy authorizationPolicy;

    public ReportingController(ContentHealthReportService contentHealthReportService, AuditService auditService, AuthorizationPolicy authorizationPolicy) {
        this.contentHealthReportService = contentHealthReportService;
        this.auditService = auditService;
        this.authorizationPolicy = authorizationPolicy;
    }

    @GetMapping("/reports/content-health")
    public ContentHealthReport contentHealth(CmsSecurityContext context) {
        return contentHealthReportService.report(context);
    }

    @GetMapping("/audit-events")
    public AuditEventListResponse auditEvents(
        @RequestParam(required = false) String actorUserId,
        @RequestParam(required = false) String targetType,
        @RequestParam(required = false) String targetId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        CmsSecurityContext context
    ) {
        if (!authorizationPolicy.canViewAudit(context)) {
            throw ApiExceptionHandler.ApiException.forbidden("Only auditors or administrators can read the audit log");
        }
        return auditService.search(actorUserId, targetType, targetId, action, from, to, page, size);
    }
}

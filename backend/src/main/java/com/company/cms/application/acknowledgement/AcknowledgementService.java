package com.company.cms.application.acknowledgement;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.AcknowledgementReport;
import com.company.cms.api.dto.CmsDtos.AcknowledgementReportItem;
import com.company.cms.api.dto.CmsDtos.UserAcknowledgementItem;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.application.security.AudienceResolver;
import com.company.cms.domain.acknowledgement.Acknowledgement;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.common.CmsEnums.ContentType;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import com.company.cms.security.CmsSecurityContext.Personas;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AcknowledgementService {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;
    private final CmsMapper mapper;
    private final AudienceResolver audienceResolver;

    public AcknowledgementService(
        ContentRepository contentRepository,
        AuthorizationPolicy authorizationPolicy,
        AuditService auditService,
        CmsMapper mapper,
        AudienceResolver audienceResolver
    ) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
        this.mapper = mapper;
        this.audienceResolver = audienceResolver;
    }

    public Acknowledgement acknowledge(String contentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canViewPortalContent(context, item)) {
            auditService.record(context.currentUser().id(), "DENIED", "CONTENT_ITEM", item.getId(), "Denied notice acknowledgement");
            throw ApiException.forbidden("Only eligible employees can acknowledge this notice");
        }
        if (item.getContentType() != ContentType.NOTICE || !item.isRequiresAcknowledgement()) {
            throw ApiException.badRequest("ACK_NOT_REQUIRED", "This content does not require acknowledgement");
        }
        if (item.getCurrentPublishedVersionId() == null) {
            throw ApiException.conflict("This notice has no published version to acknowledge");
        }
        var acknowledgement = Acknowledgement.portal(item.getId(), item.getCurrentPublishedVersionId(), context.currentUser().id());
        contentRepository.saveAcknowledgement(acknowledgement);
        auditService.record(context.currentUser().id(), "ACKNOWLEDGED", "CONTENT_ITEM", item.getId(), "Acknowledged required notice");
        return acknowledgement;
    }

    public List<UserAcknowledgementItem> myAcknowledgements(String status, CmsSecurityContext context) {
        return contentRepository.list().stream()
            .filter(item -> item.isPortalVisibleNow() && item.isRequiresAcknowledgement() && authorizationPolicy.canViewPortalContent(context, item))
            .map(item -> {
                var acknowledgement = contentRepository.findAcknowledgement(item.getId(), item.getCurrentPublishedVersionId(), context.currentUser().id());
                String currentStatus = acknowledgement.isPresent() ? "COMPLETED" : "PENDING";
                return new UserAcknowledgementItem(
                    mapper.toSummary(item, context.currentUser().id()),
                    currentStatus,
                    acknowledgement.map(Acknowledgement::acknowledgedAt).orElse(null),
                    item.getExpiresAt()
                );
            })
            .filter(item -> status == null || item.status().equalsIgnoreCase(status))
            .toList();
    }

    public AcknowledgementReport report(String contentId, String department, String status, CmsSecurityContext context) {
        if (!authorizationPolicy.canViewReports(context)) {
            throw ApiException.forbidden("Only notice managers, editors, auditors, or administrators can view acknowledgement reports");
        }
        var item = contentRepository.get(contentId);
        var users = Personas.allUsers().stream()
            .filter(user -> department == null || user.department().equalsIgnoreCase(department))
            .filter(user -> user.roles().stream().anyMatch(role -> role.name().equals("EMPLOYEE")))
            .filter(user -> audienceResolver.isTargetUser(user, item.getAudienceId()))
            .toList();
        var allRows = users.stream()
            .map(user -> {
                var acknowledgement = contentRepository.findAcknowledgement(item.getId(), item.getCurrentPublishedVersionId(), user.id());
                String currentStatus = acknowledgement.isPresent() ? "COMPLETED" : "PENDING";
                return new AcknowledgementReportItem(user.toRef(), user.department(), currentStatus, acknowledgement.map(Acknowledgement::acknowledgedAt).orElse(null));
            }).toList();
        int acknowledgedCount = (int) allRows.stream().filter(row -> row.status().equals("COMPLETED")).count();
        int targeted = allRows.size();
        var rows = allRows.stream()
            .filter(row -> status == null || row.status().equalsIgnoreCase(status))
            .toList();
        int pending = targeted - acknowledgedCount;
        double rate = targeted == 0 ? 0 : (double) acknowledgedCount / targeted;
        return new AcknowledgementReport(item.getId(), targeted, acknowledgedCount, pending, rate, rows);
    }
}

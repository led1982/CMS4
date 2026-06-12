package com.company.cms.application.reporting;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.ContentHealthReport;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.content.ContentItem;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

@Service
public class ContentHealthReportService {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final CmsMapper mapper;

    public ContentHealthReportService(ContentRepository contentRepository, AuthorizationPolicy authorizationPolicy, CmsMapper mapper) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.mapper = mapper;
    }

    public ContentHealthReport report(CmsSecurityContext context) {
        if (!authorizationPolicy.canViewReports(context)) {
            throw ApiException.forbidden("Only content operations users can view content health reports");
        }
        Instant now = Instant.now();
        return new ContentHealthReport(
            summarize(context, item -> item.getStatus() == ContentStatus.PUBLISHED && item.getUpdatedAt().isBefore(now.minus(180, ChronoUnit.DAYS))),
            summarize(context, item -> item.getStatus() == ContentStatus.PUBLISHED && item.getExpiresAt() != null && !item.getExpiresAt().isAfter(now.plus(30, ChronoUnit.DAYS))),
            summarize(context, item -> item.getStatus() == ContentStatus.ARCHIVED || item.getStatus() == ContentStatus.EXPIRED),
            summarize(context, item -> item.getStatus() == ContentStatus.PUBLISHED && item.getPublishedAt() != null && !item.getPublishedAt().isBefore(now.minus(30, ChronoUnit.DAYS)))
        );
    }

    private java.util.List<com.company.cms.api.dto.CmsDtos.ContentSummary> summarize(CmsSecurityContext context, Predicate<ContentItem> predicate) {
        return contentRepository.list().stream()
            .filter(predicate)
            .sorted(Comparator.comparing(ContentItem::getUpdatedAt).reversed())
            .limit(50)
            .map(item -> mapper.toSummary(item, context.currentUser().id()))
            .toList();
    }
}

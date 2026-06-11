package com.company.cms.application.portal;

import com.company.cms.api.dto.CmsDtos.PortalHome;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.domain.common.CmsEnums.Priority;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.infrastructure.persistence.GovernanceRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class PortalHomeService {
    private final ContentRepository contentRepository;
    private final GovernanceRepository governanceRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final CmsMapper mapper;

    public PortalHomeService(ContentRepository contentRepository, GovernanceRepository governanceRepository, AuthorizationPolicy authorizationPolicy, CmsMapper mapper) {
        this.contentRepository = contentRepository;
        this.governanceRepository = governanceRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.mapper = mapper;
    }

    public PortalHome home(CmsSecurityContext context) {
        var visible = contentRepository.list().stream()
            .filter(item -> authorizationPolicy.canViewPortalContent(context, item))
            .toList();
        var required = visible.stream()
            .filter(item -> item.isRequiresAcknowledgement())
            .map(item -> mapper.toSummary(item, context.currentUser().id()))
            .limit(6)
            .toList();
        var pinned = visible.stream()
            .filter(item -> item.getPriority() == Priority.PINNED || item.getPriority() == Priority.URGENT)
            .map(item -> mapper.toSummary(item, context.currentUser().id()))
            .limit(6)
            .toList();
        var recent = visible.stream()
            .sorted(Comparator.comparing(com.company.cms.domain.content.ContentItem::getUpdatedAt).reversed())
            .map(item -> mapper.toSummary(item, context.currentUser().id()))
            .limit(10)
            .toList();
        var categories = governanceRepository.categoryTree(false).stream()
            .map(this::toCategory)
            .toList();
        return new PortalHome(required, pinned, recent, categories);
    }

    private com.company.cms.api.dto.CmsDtos.Category toCategory(GovernanceRepository.CategoryNode node) {
        return mapper.toCategory(node.category(), node.children().stream().map(this::toCategory).toList());
    }
}

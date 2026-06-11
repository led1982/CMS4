package com.company.cms.application.search;

import com.company.cms.api.dto.CmsDtos.ContentListResponse;
import com.company.cms.api.dto.CmsDtos.PageMeta;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.common.CmsEnums.ContentType;
import com.company.cms.domain.content.ContentItem;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
public class ContentSearchService {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final SearchTextNormalizer normalizer;
    private final CmsMapper mapper;

    public ContentSearchService(ContentRepository contentRepository, AuthorizationPolicy authorizationPolicy, SearchTextNormalizer normalizer, CmsMapper mapper) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.normalizer = normalizer;
        this.mapper = mapper;
    }

    public ContentListResponse search(
        String query,
        ContentType contentType,
        String categoryId,
        String tag,
        ContentStatus status,
        Boolean acknowledgementRequired,
        int page,
        int size,
        CmsSecurityContext context
    ) {
        var filtered = contentRepository.list().stream()
            .filter(item -> status == null ? authorizationPolicy.canViewPortalContent(context, item) : authorizationPolicy.canViewContent(context, item) && item.getStatus() == status)
            .filter(item -> contentType == null || item.getContentType() == contentType)
            .filter(item -> categoryId == null || item.getCategoryId().equals(categoryId))
            .filter(item -> tag == null || item.getTags().stream().anyMatch(value -> value.equalsIgnoreCase(tag)))
            .filter(item -> acknowledgementRequired == null || item.isRequiresAcknowledgement() == acknowledgementRequired)
            .filter(item -> matches(item, query))
            .sorted(Comparator.comparing(ContentItem::getPriority).reversed()
                .thenComparing(ContentItem::getUpdatedAt, Comparator.reverseOrder()))
            .toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        var items = filtered.subList(from, to).stream()
            .map(item -> mapper.toSummary(item, context.currentUser().id()))
            .toList();
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);
        return new ContentListResponse(items, new PageMeta(page, size, filtered.size(), totalPages));
    }

    private boolean matches(ContentItem item, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String text = String.join(" ", item.getTitle(), item.getSummary() == null ? "" : item.getSummary(), item.getBody() == null ? "" : item.getBody(), String.join(" ", item.getTags()));
        return normalizer.containsNormalized(text, query);
    }
}

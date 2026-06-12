package com.company.cms.application.portal;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.Bookmark;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BookmarkService {
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;
    private final CmsMapper mapper;

    public BookmarkService(ContentRepository contentRepository, AuthorizationPolicy authorizationPolicy, AuditService auditService, CmsMapper mapper) {
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    public List<Bookmark> list(CmsSecurityContext context) {
        return contentRepository.listBookmarks(context.currentUser().id()).stream()
            .map(record -> {
                var item = contentRepository.get(record.contentItemId());
                if (!authorizationPolicy.canViewPortalContent(context, item)) {
                    return null;
                }
                return new Bookmark(
                    context.currentUser().id() + ":" + item.getId(),
                    mapper.toSummary(item, context.currentUser().id()),
                    record.savedAt()
                );
            })
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    public Bookmark create(String contentId, CmsSecurityContext context) {
        var item = contentRepository.get(contentId);
        if (!authorizationPolicy.canViewPortalContent(context, item)) {
            auditService.record(context.currentUser().id(), "DENIED", "CONTENT_ITEM", contentId, "Denied bookmark creation");
            throw ApiException.forbidden("Only visible portal content can be bookmarked");
        }
        var savedAt = contentRepository.saveBookmark(context.currentUser().id(), contentId);
        auditService.record(context.currentUser().id(), "BOOKMARKED", "CONTENT_ITEM", contentId, "Bookmarked portal content");
        return new Bookmark(context.currentUser().id() + ":" + contentId, mapper.toSummary(item, context.currentUser().id()), savedAt);
    }

    public void delete(String contentId, CmsSecurityContext context) {
        contentRepository.deleteBookmark(context.currentUser().id(), contentId);
        auditService.record(context.currentUser().id(), "BOOKMARK_REMOVED", "CONTENT_ITEM", contentId, "Removed portal bookmark");
    }
}

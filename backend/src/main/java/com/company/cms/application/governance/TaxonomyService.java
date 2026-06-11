package com.company.cms.application.governance;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.CategoryCreateRequest;
import com.company.cms.api.dto.CmsDtos.CategoryUpdateRequest;
import com.company.cms.api.dto.CmsDtos.TagCreateRequest;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.governance.Category;
import com.company.cms.infrastructure.persistence.ContentRepository;
import com.company.cms.infrastructure.persistence.GovernanceRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaxonomyService {
    private final GovernanceRepository governanceRepository;
    private final ContentRepository contentRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;

    public TaxonomyService(GovernanceRepository governanceRepository, ContentRepository contentRepository, AuthorizationPolicy authorizationPolicy, AuditService auditService) {
        this.governanceRepository = governanceRepository;
        this.contentRepository = contentRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
    }

    public List<GovernanceRepository.CategoryNode> categories(boolean includeInactive) {
        return governanceRepository.categoryTree(includeInactive);
    }

    public Category createCategory(CategoryCreateRequest request, CmsSecurityContext context) {
        requireAdmin(context);
        if (governanceRepository.categoryNameExists(request.parentId(), request.name(), "")) {
            throw ApiException.conflict("A category with this name already exists under the same parent");
        }
        var category = Category.create(
            request.parentId(),
            request.name(),
            request.description(),
            request.ownerUserId(),
            request.defaultAudienceId(),
            request.defaultReviewerGroupId(),
            request.sortOrder() == null ? 0 : request.sortOrder()
        );
        governanceRepository.saveCategory(category);
        auditService.record(context.currentUser().id(), "TAXONOMY_CHANGED", "CATEGORY", category.id(), "Created category");
        return category;
    }

    public Category updateCategory(String categoryId, CategoryUpdateRequest request, CmsSecurityContext context) {
        requireAdmin(context);
        var current = governanceRepository.getCategory(categoryId);
        if (governanceRepository.categoryNameExists(request.parentId(), request.name(), categoryId)) {
            throw ApiException.conflict("A category with this name already exists under the same parent");
        }
        if (Boolean.FALSE.equals(request.active()) && isReferencedByPublishedContent(categoryId)) {
            throw ApiException.conflict("Category is referenced by content; reassign or archive content before deactivation");
        }
        var updated = current.update(
            request.parentId(),
            request.name(),
            request.description(),
            request.ownerUserId(),
            request.defaultAudienceId(),
            request.defaultReviewerGroupId(),
            request.sortOrder() == null ? current.sortOrder() : request.sortOrder(),
            request.active() == null ? current.active() : request.active()
        );
        governanceRepository.saveCategory(updated);
        auditService.record(context.currentUser().id(), "TAXONOMY_CHANGED", "CATEGORY", updated.id(), "Updated category");
        return updated;
    }

    public List<Category.Tag> tags() {
        return governanceRepository.listTags();
    }

    public Category.Tag createTag(TagCreateRequest request, CmsSecurityContext context) {
        requireAdmin(context);
        String normalized = request.name().trim().toLowerCase();
        if (governanceRepository.tagExists(normalized)) {
            throw ApiException.conflict("A tag with this name already exists");
        }
        var tag = Category.Tag.create(request.name(), request.description());
        governanceRepository.saveTag(tag);
        auditService.record(context.currentUser().id(), "TAXONOMY_CHANGED", "TAG", tag.id(), "Created tag");
        return tag;
    }

    private boolean isReferencedByPublishedContent(String categoryId) {
        return contentRepository.list().stream().anyMatch(item -> item.getCategoryId().equals(categoryId));
    }

    private void requireAdmin(CmsSecurityContext context) {
        if (!authorizationPolicy.canAdminister(context)) {
            throw ApiException.forbidden("Only administrators can manage taxonomy");
        }
    }
}

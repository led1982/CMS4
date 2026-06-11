package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.Category;
import com.company.cms.api.dto.CmsDtos.CategoryCreateRequest;
import com.company.cms.api.dto.CmsDtos.CategoryUpdateRequest;
import com.company.cms.api.dto.CmsDtos.Tag;
import com.company.cms.api.dto.CmsDtos.TagCreateRequest;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.application.governance.TaxonomyService;
import com.company.cms.infrastructure.persistence.GovernanceRepository.CategoryNode;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TaxonomyController {
    private final TaxonomyService taxonomyService;
    private final CmsMapper mapper;

    public TaxonomyController(TaxonomyService taxonomyService, CmsMapper mapper) {
        this.taxonomyService = taxonomyService;
        this.mapper = mapper;
    }

    @GetMapping("/categories")
    public List<Category> categories(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return taxonomyService.categories(includeInactive).stream().map(this::toCategory).toList();
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryCreateRequest request, CmsSecurityContext context) {
        var category = taxonomyService.createCategory(request, context);
        return ResponseEntity.created(URI.create("/api/v1/categories/" + category.id()))
            .body(mapper.toCategory(category, List.of()));
    }

    @PatchMapping("/categories/{categoryId}")
    public Category updateCategory(@PathVariable String categoryId, @Valid @RequestBody CategoryUpdateRequest request, CmsSecurityContext context) {
        return mapper.toCategory(taxonomyService.updateCategory(categoryId, request, context), List.of());
    }

    @GetMapping("/tags")
    public List<Tag> tags() {
        return taxonomyService.tags().stream().map(mapper::toTag).toList();
    }

    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@Valid @RequestBody TagCreateRequest request, CmsSecurityContext context) {
        var tag = taxonomyService.createTag(request, context);
        return ResponseEntity.created(URI.create("/api/v1/tags/" + tag.id())).body(mapper.toTag(tag));
    }

    private Category toCategory(CategoryNode node) {
        return mapper.toCategory(node.category(), node.children().stream().map(this::toCategory).toList());
    }
}

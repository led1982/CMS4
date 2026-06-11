package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.ContentCreateRequest;
import com.company.cms.api.dto.CmsDtos.ContentDetail;
import com.company.cms.api.dto.CmsDtos.ContentListResponse;
import com.company.cms.api.dto.CmsDtos.ContentUpdateRequest;
import com.company.cms.api.dto.CmsDtos.ContentVersion;
import com.company.cms.api.dto.CmsDtos.PublishRequest;
import com.company.cms.api.dto.CmsDtos.SubmitReviewRequest;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.application.content.ContentWorkflowService;
import com.company.cms.application.search.ContentSearchService;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.common.CmsEnums.ContentType;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/content")
public class ContentController {
    private final ContentWorkflowService workflowService;
    private final ContentSearchService searchService;
    private final CmsMapper mapper;

    public ContentController(ContentWorkflowService workflowService, ContentSearchService searchService, CmsMapper mapper) {
        this.workflowService = workflowService;
        this.searchService = searchService;
        this.mapper = mapper;
    }

    @GetMapping
    public ContentListResponse search(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) ContentType contentType,
        @RequestParam(required = false) String categoryId,
        @RequestParam(required = false) String tag,
        @RequestParam(required = false) ContentStatus status,
        @RequestParam(required = false) Boolean acknowledgementRequired,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        CmsSecurityContext context
    ) {
        return searchService.search(q, contentType, categoryId, tag, status, acknowledgementRequired, page, size, context);
    }

    @PostMapping
    public ResponseEntity<ContentDetail> create(@Valid @RequestBody ContentCreateRequest request, CmsSecurityContext context) {
        var item = workflowService.create(request, context);
        return ResponseEntity.created(URI.create("/api/v1/content/" + item.getId()))
            .body(mapper.toDetail(item, context.currentUser().id()));
    }

    @GetMapping("/{contentId}")
    public ContentDetail get(@PathVariable String contentId, CmsSecurityContext context) {
        return mapper.toDetail(workflowService.getForUser(contentId, context), context.currentUser().id());
    }

    @PatchMapping("/{contentId}")
    public ContentDetail update(@PathVariable String contentId, @Valid @RequestBody ContentUpdateRequest request, CmsSecurityContext context) {
        return mapper.toDetail(workflowService.update(contentId, request, context), context.currentUser().id());
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> delete(@PathVariable String contentId, CmsSecurityContext context) {
        workflowService.delete(contentId, context);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contentId}/submit-review")
    public ContentDetail submitForReview(@PathVariable String contentId, @Valid @RequestBody SubmitReviewRequest request, CmsSecurityContext context) {
        return mapper.toDetail(workflowService.submitForReview(contentId, request, context), context.currentUser().id());
    }

    @PostMapping("/{contentId}/publish")
    public ContentDetail publish(@PathVariable String contentId, @Valid @RequestBody PublishRequest request, CmsSecurityContext context) {
        return mapper.toDetail(workflowService.publish(contentId, request, context), context.currentUser().id());
    }

    @PostMapping("/{contentId}/archive")
    public ContentDetail archive(@PathVariable String contentId, CmsSecurityContext context) {
        return mapper.toDetail(workflowService.archive(contentId, context), context.currentUser().id());
    }

    @GetMapping("/{contentId}/versions")
    public List<ContentVersion> versions(@PathVariable String contentId, CmsSecurityContext context) {
        return workflowService.versions(contentId, context).stream().map(mapper::toVersion).toList();
    }
}

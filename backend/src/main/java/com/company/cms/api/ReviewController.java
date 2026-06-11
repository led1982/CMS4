package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.ContentSummary;
import com.company.cms.api.dto.CmsDtos.ReviewDecisionRequest;
import com.company.cms.api.dto.CmsDtos.ContentDetail;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.application.review.ReviewService;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {
    private final ReviewService reviewService;
    private final CmsMapper mapper;

    public ReviewController(ReviewService reviewService, CmsMapper mapper) {
        this.reviewService = reviewService;
        this.mapper = mapper;
    }

    @PostMapping("/content/{contentId}/review")
    public ContentDetail review(@PathVariable String contentId, @Valid @RequestBody ReviewDecisionRequest request, CmsSecurityContext context) {
        return mapper.toDetail(reviewService.review(contentId, request, context), context.currentUser().id());
    }

    @GetMapping("/review/queue")
    public List<ContentSummary> queue(CmsSecurityContext context) {
        return reviewService.queue(context).stream()
            .map(item -> mapper.toSummary(item, context.currentUser().id()))
            .toList();
    }
}

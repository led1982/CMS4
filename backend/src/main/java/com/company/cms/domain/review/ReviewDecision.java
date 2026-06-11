package com.company.cms.domain.review;

import com.company.cms.domain.common.CmsEnums.ReviewDecisionState;
import java.time.Instant;
import java.util.UUID;

public record ReviewDecision(
    String id,
    String contentItemId,
    String versionId,
    String reviewerUserId,
    ReviewDecisionState decision,
    String comments,
    Instant decidedAt
) {
    public static ReviewDecision create(String contentItemId, String versionId, String reviewerUserId, ReviewDecisionState decision, String comments) {
        return new ReviewDecision(UUID.randomUUID().toString(), contentItemId, versionId, reviewerUserId, decision, comments, Instant.now());
    }
}

package com.company.cms.domain.acknowledgement;

import java.time.Instant;
import java.util.UUID;

public record Acknowledgement(
    String id,
    String contentItemId,
    String versionId,
    String userId,
    Instant acknowledgedAt,
    String source
) {
    public static Acknowledgement portal(String contentItemId, String versionId, String userId) {
        return new Acknowledgement(UUID.randomUUID().toString(), contentItemId, versionId, userId, Instant.now(), "PORTAL");
    }
}

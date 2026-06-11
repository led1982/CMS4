package com.company.cms.domain.governance;

import com.company.cms.domain.common.CmsEnums.AudienceType;
import com.company.cms.domain.common.CmsEnums.RoleCode;
import com.company.cms.domain.content.ContentItem;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Category(
    String id,
    String parentId,
    String name,
    String slug,
    String description,
    String ownerUserId,
    String defaultAudienceId,
    String defaultReviewerGroupId,
    int sortOrder,
    boolean active
) {
    public static Category create(
        String parentId,
        String name,
        String description,
        String ownerUserId,
        String defaultAudienceId,
        String defaultReviewerGroupId,
        int sortOrder
    ) {
        return new Category(
            UUID.randomUUID().toString(),
            parentId,
            name,
            ContentItem.slugify(name),
            description,
            ownerUserId,
            defaultAudienceId,
            defaultReviewerGroupId,
            sortOrder,
            true
        );
    }

    public Category update(
        String parentId,
        String name,
        String description,
        String ownerUserId,
        String defaultAudienceId,
        String defaultReviewerGroupId,
        int sortOrder,
        boolean active
    ) {
        return new Category(id, parentId, name, ContentItem.slugify(name), description, ownerUserId, defaultAudienceId, defaultReviewerGroupId, sortOrder, active);
    }

    public record Tag(String id, String name, String normalizedName, String description, boolean active) {
        public static Tag create(String name, String description) {
            return new Tag(UUID.randomUUID().toString(), name, name.trim().toLowerCase(), description, true);
        }
    }

    public record Audience(String id, String name, AudienceType audienceType, Map<String, Object> criteria, boolean active) {
        public static Audience create(String name, AudienceType audienceType, Map<String, Object> criteria) {
            return new Audience(UUID.randomUUID().toString(), name, audienceType, criteria, true);
        }
    }

    public record RoleAssignment(
        String id,
        String userId,
        RoleCode role,
        String scopeType,
        String scopeId,
        Instant assignedAt,
        Instant expiresAt
    ) {
        public static RoleAssignment create(String userId, RoleCode role, String scopeType, String scopeId, Instant expiresAt) {
            return new RoleAssignment(UUID.randomUUID().toString(), userId, role, scopeType, scopeId, Instant.now(), expiresAt);
        }
    }
}

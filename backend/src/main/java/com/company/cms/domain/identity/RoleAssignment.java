package com.company.cms.domain.identity;

import com.company.cms.domain.common.CmsEnums.RoleCode;
import java.time.Instant;

public record RoleAssignment(
    String id,
    String userId,
    RoleCode role,
    String scopeType,
    String scopeId,
    Instant assignedAt,
    Instant expiresAt
) {
}

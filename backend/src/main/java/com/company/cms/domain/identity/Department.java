package com.company.cms.domain.identity;

import java.time.Instant;

public record Department(
    String id,
    String name,
    String code,
    String parentDepartmentId,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}

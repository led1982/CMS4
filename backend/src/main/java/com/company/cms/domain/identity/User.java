package com.company.cms.domain.identity;

import java.time.Instant;

public record User(
    String id,
    String employeeId,
    String displayName,
    String email,
    String departmentId,
    String status,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt
) {
}

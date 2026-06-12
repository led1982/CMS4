package com.company.cms.application.security;

import com.company.cms.domain.common.CmsEnums.RoleCode;
import com.company.cms.domain.governance.Category.Audience;
import com.company.cms.infrastructure.persistence.GovernanceRepository;
import com.company.cms.security.CmsSecurityContext;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AudienceResolver {
    private final GovernanceRepository governanceRepository;

    public AudienceResolver(GovernanceRepository governanceRepository) {
        this.governanceRepository = governanceRepository;
    }

    public boolean isMember(CmsSecurityContext context, String audienceId) {
        if (audienceId == null || audienceId.isBlank()) {
            return false;
        }
        Audience audience = governanceRepository.getAudience(audienceId);
        if (!audience.active()) {
            return false;
        }
        return switch (audience.audienceType()) {
            case ALL_COMPANY -> true;
            case DEPARTMENT -> matchesDepartment(context, audience);
            case ROLE -> matchesRole(context, audience);
            case CUSTOM -> matchesCustom(context, audience);
            case GROUP -> matchesDepartment(context, audience) || matchesRole(context, audience);
        };
    }

    public boolean isTargetUser(CmsSecurityContext.UserAccount user, String audienceId) {
        if (audienceId == null || audienceId.isBlank()) {
            return false;
        }
        Audience audience = governanceRepository.getAudience(audienceId);
        if (!audience.active()) {
            return false;
        }
        return switch (audience.audienceType()) {
            case ALL_COMPANY -> true;
            case DEPARTMENT -> stringMatches(audience.criteria().get("department"), user.department())
                || collectionContains(audience.criteria().get("departments"), user.department());
            case ROLE -> roleMatches(audience, user);
            case CUSTOM -> stringMatches(audience.criteria().get("userId"), user.id())
                || collectionContains(audience.criteria().get("userIds"), user.id())
                || collectionContains(audience.criteria().get("users"), user.id());
            case GROUP -> stringMatches(audience.criteria().get("department"), user.department())
                || collectionContains(audience.criteria().get("departments"), user.department())
                || roleMatches(audience, user);
        };
    }

    private boolean matchesDepartment(CmsSecurityContext context, Audience audience) {
        return stringMatches(audience.criteria().get("department"), context.currentUser().department())
            || collectionContains(audience.criteria().get("departments"), context.currentUser().department());
    }

    private boolean matchesRole(CmsSecurityContext context, Audience audience) {
        return roleMatches(audience, context.currentUser());
    }

    private boolean matchesCustom(CmsSecurityContext context, Audience audience) {
        return stringMatches(audience.criteria().get("userId"), context.currentUser().id())
            || collectionContains(audience.criteria().get("userIds"), context.currentUser().id())
            || collectionContains(audience.criteria().get("users"), context.currentUser().id());
    }

    private boolean roleMatches(Audience audience, CmsSecurityContext.UserAccount user) {
        Object role = audience.criteria().get("role");
        Object roles = audience.criteria().get("roles");
        return user.roles().stream()
            .map(RoleCode::name)
            .anyMatch(value -> stringMatches(role, value) || collectionContains(roles, value));
    }

    private boolean stringMatches(Object expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return normalize(expected.toString()).equals(normalize(actual));
    }

    private boolean collectionContains(Object values, String actual) {
        if (!(values instanceof Collection<?> collection) || actual == null) {
            return false;
        }
        String normalizedActual = normalize(actual);
        return collection.stream()
            .filter(Objects::nonNull)
            .map(value -> normalize(value.toString()))
            .anyMatch(normalizedActual::equals);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}

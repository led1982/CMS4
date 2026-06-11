package com.company.cms.security;

import com.company.cms.api.dto.CmsDtos.UserProfile;
import com.company.cms.api.dto.CmsDtos.UserRef;
import com.company.cms.domain.common.CmsEnums.RoleCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class CmsSecurityContext {
    private final UserAccount currentUser;

    public CmsSecurityContext(HttpServletRequest request) {
        String persona = request.getHeader("X-CMS-User");
        if (persona == null || persona.isBlank()) {
            persona = "employee";
        }
        this.currentUser = Personas.byKey(persona);
    }

    public UserAccount currentUser() {
        return currentUser;
    }

    public boolean hasAnyRole(RoleCode... roles) {
        for (RoleCode role : roles) {
            if (currentUser.roles().contains(role)) {
                return true;
            }
        }
        return false;
    }

    public UserProfile profile() {
        return new UserProfile(
            currentUser.id(),
            currentUser.employeeId(),
            currentUser.displayName(),
            currentUser.email(),
            currentUser.department(),
            currentUser.roles(),
            Personas.capabilities(currentUser.roles())
        );
    }

    public UserRef currentUserRef() {
        return currentUser.toRef();
    }

    public record UserAccount(
        String id,
        String employeeId,
        String displayName,
        String email,
        String department,
        List<RoleCode> roles
    ) {
        public UserRef toRef() {
            return new UserRef(id, displayName, email, department);
        }
    }

    public static final class Personas {
        private static final Map<String, UserAccount> USERS = Map.of(
            "employee", new UserAccount("employee", "E001", "Employee User", "employee@example.com", "Operations", List.of(RoleCode.EMPLOYEE)),
            "author", new UserAccount("author", "A001", "Author User", "author@example.com", "People", List.of(RoleCode.EMPLOYEE, RoleCode.AUTHOR)),
            "reviewer", new UserAccount("reviewer", "R001", "Reviewer User", "reviewer@example.com", "People", List.of(RoleCode.EMPLOYEE, RoleCode.REVIEWER)),
            "editor", new UserAccount("editor", "ED01", "Editor User", "editor@example.com", "Communications", List.of(RoleCode.EMPLOYEE, RoleCode.EDITOR)),
            "admin", new UserAccount("admin", "AD01", "Admin User", "admin@example.com", "IT", List.of(RoleCode.EMPLOYEE, RoleCode.AUTHOR, RoleCode.REVIEWER, RoleCode.EDITOR, RoleCode.ADMINISTRATOR))
        );

        private Personas() {
        }

        public static UserAccount byKey(String key) {
            return USERS.getOrDefault(key.toLowerCase(Locale.ROOT), USERS.get("employee"));
        }

        public static UserRef ref(String userId) {
            return USERS.values().stream()
                .filter(user -> user.id().equals(userId))
                .findFirst()
                .orElse(USERS.get("employee"))
                .toRef();
        }

        public static List<UserAccount> allUsers() {
            return USERS.values().stream()
                .sorted(Comparator.comparing(UserAccount::id))
                .toList();
        }

        public static List<String> capabilities(List<RoleCode> roles) {
            var capabilities = new java.util.LinkedHashSet<String>();
            capabilities.add("PORTAL_READ");
            if (roles.contains(RoleCode.AUTHOR) || roles.contains(RoleCode.EDITOR) || roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("CONTENT_CREATE");
                capabilities.add("CONTENT_EDIT");
            }
            if (roles.contains(RoleCode.REVIEWER) || roles.contains(RoleCode.EDITOR) || roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("CONTENT_REVIEW");
                capabilities.add("CONTENT_PUBLISH");
            }
            if (roles.contains(RoleCode.EDITOR) || roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("ACK_REPORT");
            }
            if (roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("ADMIN_TAXONOMY");
                capabilities.add("ADMIN_ACCESS");
                capabilities.add("AUDIT_READ");
            }
            return List.copyOf(capabilities);
        }
    }
}

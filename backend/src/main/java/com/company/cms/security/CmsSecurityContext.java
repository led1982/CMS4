package com.company.cms.security;

import com.company.cms.api.dto.CmsDtos.UserProfile;
import com.company.cms.api.dto.CmsDtos.UserRef;
import com.company.cms.domain.common.CmsEnums.RoleCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
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
        private static final UserAccount EMPLOYEE = new UserAccount("employee", "E001", "Employee User", "employee@example.com", "Operations", List.of(RoleCode.EMPLOYEE));
        private static final UserAccount AUTHOR = new UserAccount("author", "A001", "Author User", "author@example.com", "People", List.of(RoleCode.EMPLOYEE, RoleCode.AUTHOR));
        private static final UserAccount REVIEWER = new UserAccount("reviewer", "R001", "Reviewer User", "reviewer@example.com", "People", List.of(RoleCode.EMPLOYEE, RoleCode.REVIEWER));
        private static final UserAccount EDITOR = new UserAccount("editor", "ED01", "Editor User", "editor@example.com", "Communications", List.of(RoleCode.EMPLOYEE, RoleCode.EDITOR));
        private static final UserAccount ADMIN = new UserAccount("admin", "AD01", "Admin User", "admin@example.com", "IT", List.of(RoleCode.EMPLOYEE, RoleCode.AUTHOR, RoleCode.REVIEWER, RoleCode.EDITOR, RoleCode.ADMINISTRATOR));
        private static final UserAccount HR_EMPLOYEE = new UserAccount("employee.hr", "H001", "HR Employee", "employee.hr@example.com", "HR", List.of(RoleCode.EMPLOYEE));
        private static final UserAccount ENG_EMPLOYEE = new UserAccount("employee.eng", "N001", "Engineering Employee", "employee.eng@example.com", "Engineering", List.of(RoleCode.EMPLOYEE));
        private static final UserAccount HR_AUTHOR = new UserAccount("author.hr", "H002", "HR Author", "author.hr@example.com", "HR", List.of(RoleCode.EMPLOYEE, RoleCode.AUTHOR));
        private static final UserAccount HR_REVIEWER = new UserAccount("reviewer.hr", "H003", "HR Reviewer", "reviewer.hr@example.com", "HR", List.of(RoleCode.EMPLOYEE, RoleCode.REVIEWER));
        private static final UserAccount HR_PUBLISHER = new UserAccount("publisher.hr", "H004", "HR Publisher", "publisher.hr@example.com", "HR", List.of(RoleCode.EMPLOYEE, RoleCode.PUBLISHER));
        private static final UserAccount NOTICE_MANAGER = new UserAccount("notice.manager", "N002", "Notice Manager", "notice.manager@example.com", "Communications", List.of(RoleCode.EMPLOYEE, RoleCode.NOTICE_MANAGER, RoleCode.PUBLISHER));
        private static final UserAccount AUDITOR = new UserAccount("auditor.cms", "AU01", "CMS Auditor", "auditor.cms@example.com", "Compliance", List.of(RoleCode.EMPLOYEE, RoleCode.AUDITOR));
        private static final UserAccount SYSTEM = new UserAccount("system", "SYS", "System", "system@example.com", "Platform", List.of());

        private static final Map<String, UserAccount> USERS = Map.ofEntries(
            entry("employee", EMPLOYEE),
            entry("author", AUTHOR),
            entry("reviewer", REVIEWER),
            entry("editor", EDITOR),
            entry("admin", ADMIN),
            entry("admin.cms", ADMIN),
            entry("employee.hr", HR_EMPLOYEE),
            entry("employee.eng", ENG_EMPLOYEE),
            entry("author.hr", HR_AUTHOR),
            entry("reviewer.hr", HR_REVIEWER),
            entry("publisher.hr", HR_PUBLISHER),
            entry("notice.manager", NOTICE_MANAGER),
            entry("auditor.cms", AUDITOR),
            entry("system", SYSTEM)
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
                .distinct()
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
            if (roles.contains(RoleCode.NOTICE_MANAGER)) {
                capabilities.add("CONTENT_CREATE");
                capabilities.add("NOTICE_MANAGE");
            }
            if (roles.contains(RoleCode.REVIEWER) || roles.contains(RoleCode.EDITOR) || roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("CONTENT_REVIEW");
            }
            if (roles.contains(RoleCode.PUBLISHER) || roles.contains(RoleCode.REVIEWER) || roles.contains(RoleCode.EDITOR) || roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("CONTENT_PUBLISH");
            }
            if (roles.contains(RoleCode.NOTICE_MANAGER) || roles.contains(RoleCode.EDITOR) || roles.contains(RoleCode.ADMINISTRATOR)) {
                capabilities.add("ACK_REPORT");
            }
            if (roles.contains(RoleCode.AUDITOR)) {
                capabilities.add("AUDIT_READ");
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

package com.company.cms.infrastructure.persistence;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.domain.common.CmsEnums.AudienceType;
import com.company.cms.domain.common.CmsEnums.RoleCode;
import com.company.cms.domain.governance.Category;
import com.company.cms.domain.governance.Category.Audience;
import com.company.cms.domain.governance.Category.RoleAssignment;
import com.company.cms.domain.governance.Category.Tag;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class GovernanceRepository {
    public static final String ALL_EMPLOYEES_AUDIENCE_ID = "aud-all";
    public static final String HR_DEPARTMENT_AUDIENCE_ID = "aud-hr";
    public static final String ENGINEERING_DEPARTMENT_AUDIENCE_ID = "aud-eng";
    public static final String HR_POLICIES_CATEGORY_ID = "cat-hr";
    public static final String IT_GUIDES_CATEGORY_ID = "cat-it";
    public static final String DEFAULT_REVIEWER_GROUP_ID = "review-hr";

    private final Map<String, Category> categories = new ConcurrentHashMap<>();
    private final Map<String, Tag> tags = new ConcurrentHashMap<>();
    private final Map<String, Audience> audiences = new ConcurrentHashMap<>();
    private final Map<String, RoleAssignment> roleAssignments = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        audiences.put(ALL_EMPLOYEES_AUDIENCE_ID, new Audience(ALL_EMPLOYEES_AUDIENCE_ID, "All Employees", AudienceType.ALL_COMPANY, Map.of("scope", "all"), true));
        audiences.put(HR_DEPARTMENT_AUDIENCE_ID, new Audience(HR_DEPARTMENT_AUDIENCE_ID, "HR Department", AudienceType.DEPARTMENT, Map.of("department", "HR"), true));
        audiences.put(ENGINEERING_DEPARTMENT_AUDIENCE_ID, new Audience(ENGINEERING_DEPARTMENT_AUDIENCE_ID, "Engineering Department", AudienceType.DEPARTMENT, Map.of("department", "Engineering"), true));
        categories.put(HR_POLICIES_CATEGORY_ID, new Category(HR_POLICIES_CATEGORY_ID, null, "HR Policies", "hr-policies", "Policies, benefits, and compliance notices", "author", ALL_EMPLOYEES_AUDIENCE_ID, DEFAULT_REVIEWER_GROUP_ID, 10, true));
        categories.put(IT_GUIDES_CATEGORY_ID, new Category(IT_GUIDES_CATEGORY_ID, null, "IT Guides", "it-guides", "Internal technology guides and procedures", "admin", ALL_EMPLOYEES_AUDIENCE_ID, DEFAULT_REVIEWER_GROUP_ID, 20, true));
        categories.put("cat-it-security", new Category("cat-it-security", IT_GUIDES_CATEGORY_ID, "Security", "security", "Security guidance and required awareness notices", "admin", ALL_EMPLOYEES_AUDIENCE_ID, DEFAULT_REVIEWER_GROUP_ID, 10, true));
        tags.put("tag-policy", new Tag("tag-policy", "Policy", "policy", "Official policy content", true));
        tags.put("tag-security", new Tag("tag-security", "Security", "security", "Security awareness and IT guidance", true));
        roleAssignments.put("ra-author", new RoleAssignment("ra-author", "author", RoleCode.AUTHOR, "CATEGORY", HR_POLICIES_CATEGORY_ID, Instant.now(), null));
        roleAssignments.put("ra-author-hr", new RoleAssignment("ra-author-hr", "author.hr", RoleCode.AUTHOR, "CATEGORY", HR_POLICIES_CATEGORY_ID, Instant.now(), null));
        roleAssignments.put("ra-reviewer", new RoleAssignment("ra-reviewer", "reviewer", RoleCode.REVIEWER, "CATEGORY", HR_POLICIES_CATEGORY_ID, Instant.now(), null));
        roleAssignments.put("ra-reviewer-hr", new RoleAssignment("ra-reviewer-hr", "reviewer.hr", RoleCode.REVIEWER, "CATEGORY", HR_POLICIES_CATEGORY_ID, Instant.now(), null));
        roleAssignments.put("ra-publisher-hr", new RoleAssignment("ra-publisher-hr", "publisher.hr", RoleCode.PUBLISHER, "CATEGORY", HR_POLICIES_CATEGORY_ID, Instant.now(), null));
        roleAssignments.put("ra-notice-manager", new RoleAssignment("ra-notice-manager", "notice.manager", RoleCode.NOTICE_MANAGER, "GLOBAL", null, Instant.now(), null));
        roleAssignments.put("ra-auditor", new RoleAssignment("ra-auditor", "auditor.cms", RoleCode.AUDITOR, "GLOBAL", null, Instant.now(), null));
        roleAssignments.put("ra-editor", new RoleAssignment("ra-editor", "editor", RoleCode.EDITOR, "GLOBAL", null, Instant.now(), null));
        roleAssignments.put("ra-admin", new RoleAssignment("ra-admin", "admin", RoleCode.ADMINISTRATOR, "GLOBAL", null, Instant.now(), null));
    }

    public List<Category> listCategories(boolean includeInactive) {
        return categories.values().stream()
            .filter(category -> includeInactive || category.active())
            .sorted(Comparator.comparing(Category::sortOrder).thenComparing(Category::name))
            .toList();
    }

    public Category getCategory(String id) {
        var category = categories.get(id);
        if (category == null) {
            throw ApiException.notFound("Category was not found");
        }
        return category;
    }

    public Category saveCategory(Category category) {
        categories.put(category.id(), category);
        return category;
    }

    public boolean categoryNameExists(String parentId, String name, String excludingId) {
        return categories.values().stream()
            .anyMatch(category -> !category.id().equals(excludingId)
                && same(category.parentId(), parentId)
                && category.name().equalsIgnoreCase(name));
    }

    public List<Tag> listTags() {
        return tags.values().stream()
            .sorted(Comparator.comparing(Tag::name))
            .toList();
    }

    public Tag saveTag(Tag tag) {
        tags.put(tag.id(), tag);
        return tag;
    }

    public boolean tagExists(String normalizedName) {
        return tags.values().stream().anyMatch(tag -> tag.normalizedName().equals(normalizedName));
    }

    public List<Audience> listAudiences() {
        return audiences.values().stream()
            .sorted(Comparator.comparing(Audience::name))
            .toList();
    }

    public Audience getAudience(String id) {
        var audience = audiences.get(id);
        if (audience == null) {
            throw ApiException.notFound("Audience was not found");
        }
        return audience;
    }

    public Audience saveAudience(Audience audience) {
        audiences.put(audience.id(), audience);
        return audience;
    }

    public List<RoleAssignment> listRoleAssignments(String userId) {
        return roleAssignments.values().stream()
            .filter(assignment -> userId == null || assignment.userId().equals(userId))
            .sorted(Comparator.comparing(RoleAssignment::assignedAt).reversed())
            .toList();
    }

    public RoleAssignment saveRoleAssignment(RoleAssignment assignment) {
        roleAssignments.put(assignment.id(), assignment);
        return assignment;
    }

    public List<CategoryNode> categoryTree(boolean includeInactive) {
        var ordered = listCategories(includeInactive);
        var byParent = new LinkedHashMap<String, List<Category>>();
        for (Category category : ordered) {
            byParent.computeIfAbsent(category.parentId(), ignored -> new ArrayList<>()).add(category);
        }
        return buildNodes(null, byParent);
    }

    private List<CategoryNode> buildNodes(String parentId, Map<String, List<Category>> byParent) {
        return byParent.getOrDefault(parentId, List.of()).stream()
            .map(category -> new CategoryNode(category, buildNodes(category.id(), byParent)))
            .toList();
    }

    private boolean same(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    public record CategoryNode(Category category, List<CategoryNode> children) {
    }
}

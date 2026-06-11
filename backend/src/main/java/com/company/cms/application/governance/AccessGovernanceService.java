package com.company.cms.application.governance;

import com.company.cms.api.ApiExceptionHandler.ApiException;
import com.company.cms.api.dto.CmsDtos.AudienceCreateRequest;
import com.company.cms.api.dto.CmsDtos.RoleAssignmentCreateRequest;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.domain.governance.Category.Audience;
import com.company.cms.domain.governance.Category.RoleAssignment;
import com.company.cms.infrastructure.persistence.GovernanceRepository;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccessGovernanceService {
    private final GovernanceRepository governanceRepository;
    private final AuthorizationPolicy authorizationPolicy;
    private final AuditService auditService;

    public AccessGovernanceService(GovernanceRepository governanceRepository, AuthorizationPolicy authorizationPolicy, AuditService auditService) {
        this.governanceRepository = governanceRepository;
        this.authorizationPolicy = authorizationPolicy;
        this.auditService = auditService;
    }

    public List<Audience> audiences(CmsSecurityContext context) {
        requireAdmin(context);
        return governanceRepository.listAudiences();
    }

    public Audience createAudience(AudienceCreateRequest request, CmsSecurityContext context) {
        requireAdmin(context);
        var audience = Audience.create(request.name(), request.audienceType(), request.criteria());
        governanceRepository.saveAudience(audience);
        auditService.record(context.currentUser().id(), "AUDIENCE_CHANGED", "AUDIENCE", audience.id(), "Created audience");
        return audience;
    }

    public List<RoleAssignment> roleAssignments(String userId, CmsSecurityContext context) {
        requireAdmin(context);
        return governanceRepository.listRoleAssignments(userId);
    }

    public RoleAssignment createRoleAssignment(RoleAssignmentCreateRequest request, CmsSecurityContext context) {
        requireAdmin(context);
        if (!"GLOBAL".equals(request.scopeType()) && (request.scopeId() == null || request.scopeId().isBlank())) {
            throw ApiException.badRequest("SCOPE_REQUIRED", "Scoped role assignments require a scopeId");
        }
        var assignment = RoleAssignment.create(request.userId(), request.role(), request.scopeType(), request.scopeId(), request.expiresAt());
        governanceRepository.saveRoleAssignment(assignment);
        auditService.record(context.currentUser().id(), "ROLE_CHANGED", "ROLE_ASSIGNMENT", assignment.id(), "Created role assignment");
        return assignment;
    }

    private void requireAdmin(CmsSecurityContext context) {
        if (!authorizationPolicy.canAdminister(context)) {
            throw ApiException.forbidden("Only administrators can manage access governance");
        }
    }
}

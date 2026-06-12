package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.Audience;
import com.company.cms.api.dto.CmsDtos.AudienceCreateRequest;
import com.company.cms.api.dto.CmsDtos.AuditEventListResponse;
import com.company.cms.api.dto.CmsDtos.RoleAssignment;
import com.company.cms.api.dto.CmsDtos.RoleAssignmentCreateRequest;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.application.governance.AccessGovernanceService;
import com.company.cms.domain.audit.AuditService;
import com.company.cms.security.AuthorizationPolicy;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AccessGovernanceService accessGovernanceService;
    private final AuditService auditService;
    private final AuthorizationPolicy authorizationPolicy;
    private final CmsMapper mapper;

    public AdminController(AccessGovernanceService accessGovernanceService, AuditService auditService, AuthorizationPolicy authorizationPolicy, CmsMapper mapper) {
        this.accessGovernanceService = accessGovernanceService;
        this.auditService = auditService;
        this.authorizationPolicy = authorizationPolicy;
        this.mapper = mapper;
    }

    @GetMapping("/audiences")
    public List<Audience> audiences(CmsSecurityContext context) {
        return accessGovernanceService.audiences(context).stream().map(mapper::toAudience).toList();
    }

    @PostMapping("/audiences")
    public ResponseEntity<Audience> createAudience(@Valid @RequestBody AudienceCreateRequest request, CmsSecurityContext context) {
        var audience = accessGovernanceService.createAudience(request, context);
        return ResponseEntity.created(URI.create("/api/v1/admin/audiences/" + audience.id())).body(mapper.toAudience(audience));
    }

    @GetMapping("/role-assignments")
    public List<RoleAssignment> roleAssignments(@RequestParam(required = false) String userId, CmsSecurityContext context) {
        return accessGovernanceService.roleAssignments(userId, context).stream().map(mapper::toRoleAssignment).toList();
    }

    @PostMapping("/role-assignments")
    public ResponseEntity<RoleAssignment> createRoleAssignment(@Valid @RequestBody RoleAssignmentCreateRequest request, CmsSecurityContext context) {
        var assignment = accessGovernanceService.createRoleAssignment(request, context);
        return ResponseEntity.created(URI.create("/api/v1/admin/role-assignments/" + assignment.id())).body(mapper.toRoleAssignment(assignment));
    }

    @GetMapping("/audit-events")
    public AuditEventListResponse auditEvents(
        @RequestParam(required = false) String actorUserId,
        @RequestParam(required = false) String targetType,
        @RequestParam(required = false) String targetId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        CmsSecurityContext context
    ) {
        if (!authorizationPolicy.canViewAudit(context)) {
            throw ApiExceptionHandler.ApiException.forbidden("Only auditors or administrators can read the audit log");
        }
        return auditService.search(actorUserId, targetType, targetId, action, from, to, page, size);
    }
}

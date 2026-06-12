package com.company.cms.security;

import com.company.cms.application.security.AudienceResolver;
import com.company.cms.domain.common.CmsEnums.AttachmentValidationStatus;
import com.company.cms.domain.common.CmsEnums.ContentStatus;
import com.company.cms.domain.common.CmsEnums.RoleCode;
import com.company.cms.domain.content.Attachment;
import com.company.cms.domain.content.ContentItem;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationPolicy {
    private final AudienceResolver audienceResolver;

    public AuthorizationPolicy(AudienceResolver audienceResolver) {
        this.audienceResolver = audienceResolver;
    }

    public boolean canCreateContent(CmsSecurityContext context) {
        return context.hasAnyRole(RoleCode.AUTHOR, RoleCode.NOTICE_MANAGER, RoleCode.EDITOR, RoleCode.ADMINISTRATOR);
    }

    public boolean canReview(CmsSecurityContext context) {
        return context.hasAnyRole(RoleCode.REVIEWER, RoleCode.EDITOR, RoleCode.ADMINISTRATOR);
    }

    public boolean canPublish(CmsSecurityContext context) {
        return context.hasAnyRole(RoleCode.PUBLISHER, RoleCode.REVIEWER, RoleCode.EDITOR, RoleCode.ADMINISTRATOR);
    }

    public boolean canAdminister(CmsSecurityContext context) {
        return context.hasAnyRole(RoleCode.ADMINISTRATOR);
    }

    public boolean canViewReports(CmsSecurityContext context) {
        return context.hasAnyRole(RoleCode.NOTICE_MANAGER, RoleCode.EDITOR, RoleCode.AUDITOR, RoleCode.ADMINISTRATOR);
    }

    public boolean canViewAudit(CmsSecurityContext context) {
        return context.hasAnyRole(RoleCode.AUDITOR, RoleCode.ADMINISTRATOR);
    }

    public boolean canViewContent(CmsSecurityContext context, ContentItem item) {
        if (context.hasAnyRole(RoleCode.ADMINISTRATOR, RoleCode.EDITOR)) {
            return item.getStatus() != ContentStatus.DELETED;
        }
        if (context.hasAnyRole(RoleCode.REVIEWER, RoleCode.PUBLISHER, RoleCode.AUDITOR)
            && item.getStatus() != ContentStatus.DELETED) {
            return true;
        }
        if (context.currentUser().id().equals(item.getAuthorUserId()) || context.currentUser().id().equals(item.getOwnerUserId())) {
            return item.getStatus() != ContentStatus.DELETED;
        }
        return canViewPortalContent(context, item);
    }

    public boolean canViewPortalContent(CmsSecurityContext context, ContentItem item) {
        return item.isPortalVisibleNow() && audienceResolver.isMember(context, item.getAudienceId());
    }

    public boolean canDownloadAttachment(CmsSecurityContext context, ContentItem item, Attachment attachment) {
        return canViewContent(context, item)
            && attachment.getValidationStatus() == AttachmentValidationStatus.ACCEPTED
            && attachment.isDownloadable();
    }
}

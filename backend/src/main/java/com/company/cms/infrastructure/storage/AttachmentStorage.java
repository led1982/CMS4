package com.company.cms.infrastructure.storage;

import com.company.cms.api.dto.CmsDtos.AttachmentCreateRequest;
import com.company.cms.domain.content.Attachment;
import com.company.cms.security.CmsSecurityContext;
import java.util.List;

public interface AttachmentStorage {
    Attachment create(String contentId, AttachmentCreateRequest request, CmsSecurityContext context);

    List<Attachment> list(String contentId, CmsSecurityContext context);

    Attachment download(String contentId, String attachmentId, CmsSecurityContext context);

    void delete(String contentId, String attachmentId, CmsSecurityContext context);
}

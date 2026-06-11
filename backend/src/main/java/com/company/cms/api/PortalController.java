package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.PortalHome;
import com.company.cms.application.portal.PortalHomeService;
import com.company.cms.security.CmsSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portal")
public class PortalController {
    private final PortalHomeService portalHomeService;

    public PortalController(PortalHomeService portalHomeService) {
        this.portalHomeService = portalHomeService;
    }

    @GetMapping("/home")
    public PortalHome home(CmsSecurityContext context) {
        return portalHomeService.home(context);
    }
}

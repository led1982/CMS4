package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.UserProfile;
import com.company.cms.security.CmsSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @GetMapping("/me")
    public UserProfile me(CmsSecurityContext context) {
        return context.profile();
    }
}

package com.company.cms.security;

import com.company.cms.support.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationRegressionTest extends IntegrationTestBase {
    @Test
    void employeeCannotUseAdminOrAuthoringEndpoints() throws Exception {
        mockMvc.perform(persona(post("/api/v1/content"), "employee")
                .content(json(Map.of(
                    "contentType", "NOTICE",
                    "title", "Blocked",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-all",
                    "ownerUserId", "employee"
                ))))
            .andExpect(status().isForbidden());

        mockMvc.perform(persona(get("/api/v1/admin/audit-events"), "employee"))
            .andExpect(status().isForbidden());
    }
}

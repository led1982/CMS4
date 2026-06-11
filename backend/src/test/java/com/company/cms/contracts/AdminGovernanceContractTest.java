package com.company.cms.contracts;

import com.company.cms.support.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminGovernanceContractTest extends IntegrationTestBase {
    @Test
    void adminCanManageGovernanceAndQueryAudit() throws Exception {
        mockMvc.perform(persona(post("/api/v1/categories"), "admin")
                .content(json(Map.of("name", "Operations", "ownerUserId", "admin", "defaultAudienceId", "aud-all", "sortOrder", 30))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", equalTo("Operations")));

        mockMvc.perform(persona(post("/api/v1/tags"), "admin")
                .content(json(Map.of("name", "Runbook", "description", "Operational runbooks"))))
            .andExpect(status().isCreated());

        mockMvc.perform(persona(post("/api/v1/admin/audiences"), "admin")
                .content(json(Map.of("name", "IT Department", "audienceType", "DEPARTMENT", "criteria", Map.of("department", "IT")))))
            .andExpect(status().isCreated());

        mockMvc.perform(persona(post("/api/v1/admin/role-assignments"), "admin")
                .content(json(Map.of("userId", "author", "role", "AUTHOR", "scopeType", "GLOBAL"))))
            .andExpect(status().isCreated());

        mockMvc.perform(persona(get("/api/v1/admin/audit-events"), "admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", not(empty())));
    }
}

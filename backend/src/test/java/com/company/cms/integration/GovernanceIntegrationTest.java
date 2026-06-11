package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GovernanceIntegrationTest extends IntegrationTestBase {
    @Test
    void unsafeCategoryDeactivationIsBlockedWhenContentReferencesIt() throws Exception {
        mockMvc.perform(persona(patch("/api/v1/categories/{id}", "cat-hr"), "admin")
                .content(json(Map.of(
                    "name", "HR Policies",
                    "ownerUserId", "author",
                    "defaultAudienceId", "aud-all",
                    "sortOrder", 10,
                    "active", false
                ))))
            .andExpect(status().isConflict());
    }

    @Test
    void nonAdminCannotCreateTaxonomy() throws Exception {
        mockMvc.perform(persona(post("/api/v1/categories"), "author")
                .content(json(Map.of("name", "Blocked", "ownerUserId", "author"))))
            .andExpect(status().isForbidden());
    }
}

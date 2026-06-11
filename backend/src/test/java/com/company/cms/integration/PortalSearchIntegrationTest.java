package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortalSearchIntegrationTest extends IntegrationTestBase {
    @Test
    void searchReturnsPublishedAudienceVisibleContentOnly() throws Exception {
        mockMvc.perform(persona(get("/api/v1/content")
                .param("q", "Security")
                .param("contentType", "NOTICE")
                .param("acknowledgementRequired", "true"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", not(empty())))
            .andExpect(jsonPath("$.items[0].status", equalTo("PUBLISHED")));

        mockMvc.perform(persona(get("/api/v1/content").param("status", "DRAFT"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", empty()));
    }
}

package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AcknowledgementIntegrationTest extends IntegrationTestBase {
    @Test
    void duplicateAcknowledgementIsPreventedAndReportCountsCompletion() throws Exception {
        var search = mockMvc.perform(persona(get("/api/v1/content").param("q", "Annual Security"), "author"))
            .andReturn();
        String contentId = objectMapper.readTree(search.getResponse().getContentAsString()).get("items").get(0).get("id").asText();

        mockMvc.perform(persona(post("/api/v1/content/{id}/acknowledgements", contentId), "author"));
        mockMvc.perform(persona(post("/api/v1/content/{id}/acknowledgements", contentId), "author"))
            .andExpect(status().isConflict());

        mockMvc.perform(persona(get("/api/v1/editor/acknowledgements").param("contentId", contentId).param("status", "COMPLETED"), "editor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acknowledgedCount", greaterThanOrEqualTo(1)));
    }
}

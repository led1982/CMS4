package com.company.cms.integration;

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

class ContentWorkflowIntegrationTest extends IntegrationTestBase {
    @Test
    void draftIsHiddenUntilApprovedAndPublished() throws Exception {
        var created = mockMvc.perform(persona(post("/api/v1/content"), "author")
                .content(json(Map.of(
                    "contentType", "KNOWLEDGE_ARTICLE",
                    "title", "Hidden Draft Test",
                    "summary", "Hidden summary",
                    "body", "Hidden body",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-all",
                    "ownerUserId", "author"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        var node = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = node.get("id").asText();
        String token = node.get("versionToken").asText();

        mockMvc.perform(persona(get("/api/v1/content").param("q", "Hidden Draft Test"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", empty()));

        var submitted = mockMvc.perform(persona(post("/api/v1/content/{id}/submit-review", id), "author")
                .content(json(Map.of("versionToken", token, "changeSummary", "Lifecycle test"))))
            .andReturn();
        token = objectMapper.readTree(submitted.getResponse().getContentAsString()).get("versionToken").asText();

        var reviewed = mockMvc.perform(persona(post("/api/v1/content/{id}/review", id), "reviewer")
                .content(json(Map.of("versionToken", token, "decision", "APPROVE"))))
            .andReturn();
        token = objectMapper.readTree(reviewed.getResponse().getContentAsString()).get("versionToken").asText();

        mockMvc.perform(persona(post("/api/v1/content/{id}/publish", id), "reviewer")
                .content(json(Map.of("versionToken", token))))
            .andExpect(status().isOk());

        mockMvc.perform(persona(get("/api/v1/content").param("q", "Hidden Draft Test"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", not(empty())))
            .andExpect(jsonPath("$.items[0].status", equalTo("PUBLISHED")));
    }
}

package com.company.cms.contracts;

import com.company.cms.support.IntegrationTestBase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ContentWorkflowContractTest extends IntegrationTestBase {
    @Test
    void createUpdateSubmitReviewPublishAndArchive() throws Exception {
        var created = mockMvc.perform(persona(post("/api/v1/content"), "author")
                .content(json(Map.of(
                    "contentType", "NOTICE",
                    "title", "Quarterly Policy Notice",
                    "summary", "Policy summary",
                    "body", "Review this policy.",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-all",
                    "ownerUserId", "author",
                    "tags", List.of("Policy"),
                    "priority", "NORMAL",
                    "requiresAcknowledgement", true
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status", equalTo("DRAFT")))
            .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();
        String token = objectMapper.readTree(created.getResponse().getContentAsString()).get("versionToken").asText();

        var updated = mockMvc.perform(persona(patch("/api/v1/content/{id}", id), "author")
                .content(json(Map.of(
                    "contentType", "NOTICE",
                    "title", "Quarterly Policy Notice Updated",
                    "summary", "Updated summary",
                    "body", "Review this updated policy.",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-all",
                    "ownerUserId", "author",
                    "tags", List.of("Policy"),
                    "priority", "PINNED",
                    "requiresAcknowledgement", true,
                    "versionToken", token,
                    "changeSummary", "Updated copy"
                ))))
            .andExpect(status().isOk())
            .andReturn();
        token = objectMapper.readTree(updated.getResponse().getContentAsString()).get("versionToken").asText();

        var submitted = mockMvc.perform(persona(post("/api/v1/content/{id}/submit-review", id), "author")
                .content(json(Map.of("versionToken", token, "changeSummary", "Ready for review"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", equalTo("IN_REVIEW")))
            .andReturn();
        token = objectMapper.readTree(submitted.getResponse().getContentAsString()).get("versionToken").asText();

        var reviewed = mockMvc.perform(persona(post("/api/v1/content/{id}/review", id), "reviewer")
                .content(json(Map.of("versionToken", token, "decision", "APPROVE", "comments", "Approved"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.workflow.latestDecision", equalTo("APPROVED")))
            .andReturn();
        token = objectMapper.readTree(reviewed.getResponse().getContentAsString()).get("versionToken").asText();

        var published = mockMvc.perform(persona(post("/api/v1/content/{id}/publish", id), "reviewer")
                .content(json(Map.of("versionToken", token))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", equalTo("PUBLISHED")))
            .andReturn();
        token = objectMapper.readTree(published.getResponse().getContentAsString()).get("versionToken").asText();

        mockMvc.perform(persona(post("/api/v1/content/{id}/archive", id), "editor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", equalTo("ARCHIVED")));

        mockMvc.perform(persona(get("/api/v1/content/{id}/versions", id), "admin"))
            .andExpect(status().isOk());

        mockMvc.perform(persona(delete("/api/v1/content/{id}", id), "admin"))
            .andExpect(status().isNoContent());
    }

    @Test
    void oversizedAttachmentReturnsPayloadTooLarge() throws Exception {
        var created = mockMvc.perform(persona(post("/api/v1/content"), "author")
                .content(json(Map.of(
                    "contentType", "DOCUMENT_RECORD",
                    "title", "Attachment Limit Test",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-all",
                    "ownerUserId", "author"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(persona(post("/api/v1/content/{id}/attachments", id), "author")
                .content(json(Map.of("fileName", "large.pdf", "mimeType", "application/pdf", "sizeBytes", 11_000_000))))
            .andExpect(status().isPayloadTooLarge())
            .andExpect(jsonPath("$.code", equalTo("PAYLOAD_TOO_LARGE")));
    }
}

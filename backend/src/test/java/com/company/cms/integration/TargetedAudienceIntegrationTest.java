package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TargetedAudienceIntegrationTest extends IntegrationTestBase {
    @Test
    void targetedContentDoesNotLeakThroughSearchDetailOrDirectAttachmentDownload() throws Exception {
        var created = mockMvc.perform(persona(post("/api/v1/content"), "author.hr")
                .content(json(Map.of(
                    "contentType", "KNOWLEDGE_ARTICLE",
                    "title", "Remote Work Policy Audience Test",
                    "summary", "HR-only remote work policy.",
                    "body", "Remote work details for HR employees.",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-hr",
                    "ownerUserId", "author.hr"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        var node = objectMapper.readTree(created.getResponse().getContentAsString());
        String contentId = node.get("id").asText();
        String token = node.get("versionToken").asText();

        var attachment = mockMvc.perform(persona(post("/api/v1/content/{id}/attachments", contentId), "author.hr")
                .content(json(Map.of("fileName", "remote-work.pdf", "mimeType", "application/pdf", "sizeBytes", 1024))))
            .andExpect(status().isCreated())
            .andReturn();
        String attachmentId = objectMapper.readTree(attachment.getResponse().getContentAsString()).get("id").asText();

        var submitted = mockMvc.perform(persona(post("/api/v1/content/{id}/submit-review", contentId), "author.hr")
                .content(json(Map.of("versionToken", token, "changeSummary", "Ready for HR review"))))
            .andExpect(status().isOk())
            .andReturn();
        token = objectMapper.readTree(submitted.getResponse().getContentAsString()).get("versionToken").asText();

        var reviewed = mockMvc.perform(persona(post("/api/v1/content/{id}/review", contentId), "reviewer.hr")
                .content(json(Map.of("versionToken", token, "decision", "APPROVE", "comments", "Approved"))))
            .andExpect(status().isOk())
            .andReturn();
        token = objectMapper.readTree(reviewed.getResponse().getContentAsString()).get("versionToken").asText();

        mockMvc.perform(persona(post("/api/v1/content/{id}/publish", contentId), "publisher.hr")
                .content(json(Map.of("versionToken", token))))
            .andExpect(status().isOk());

        mockMvc.perform(persona(get("/api/v1/content").param("q", "Remote Work Policy Audience Test"), "employee.hr"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", not(empty())));

        mockMvc.perform(persona(get("/api/v1/content").param("q", "Remote Work Policy Audience Test"), "employee.eng"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", empty()));

        mockMvc.perform(persona(get("/api/v1/content/{id}", contentId), "employee.eng"))
            .andExpect(status().isForbidden());

        mockMvc.perform(persona(get("/api/v1/downloads/{attachmentId}", attachmentId), "employee.eng"))
            .andExpect(status().isForbidden());
    }
}

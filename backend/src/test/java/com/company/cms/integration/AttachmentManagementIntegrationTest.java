package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttachmentManagementIntegrationTest extends IntegrationTestBase {
    @Test
    void authorCanDeleteDraftAttachmentMetadata() throws Exception {
        var created = mockMvc.perform(persona(post("/api/v1/content"), "author")
                .content(json(Map.of(
                    "contentType", "DOCUMENT_RECORD",
                    "title", "Attachment Delete Test",
                    "summary", "Attachment delete summary",
                    "body", "Attachment delete body",
                    "categoryId", "cat-hr",
                    "audienceId", "aud-all",
                    "ownerUserId", "author"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        String contentId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        var attachment = mockMvc.perform(persona(post("/api/v1/content/{id}/attachments", contentId), "author")
                .content(json(Map.of("fileName", "delete-me.pdf", "mimeType", "application/pdf", "sizeBytes", 2048))))
            .andExpect(status().isCreated())
            .andReturn();
        String attachmentId = objectMapper.readTree(attachment.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(persona(delete("/api/v1/content/{id}/attachments/{attachmentId}", contentId, attachmentId), "author"))
            .andExpect(status().isNoContent());

        mockMvc.perform(persona(get("/api/v1/content/{id}/attachments", contentId), "author"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", empty()));
    }
}

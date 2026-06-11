package com.company.cms.contracts;

import com.company.cms.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortalContentContractTest extends IntegrationTestBase {
    @Test
    void portalHomeSearchDetailAndAttachmentDownloadMatchContract() throws Exception {
        var search = mockMvc.perform(persona(get("/api/v1/content").param("q", "Security Awareness"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items", not(empty())))
            .andReturn();
        String contentId = objectMapper.readTree(search.getResponse().getContentAsString()).get("items").get(0).get("id").asText();

        mockMvc.perform(persona(get("/api/v1/portal/home"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requiredNotices", not(empty())));

        mockMvc.perform(persona(get("/api/v1/content/{id}", contentId), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.attachments", not(empty())));

        var attachments = mockMvc.perform(persona(get("/api/v1/content/{id}/attachments", contentId), "employee"))
            .andExpect(status().isOk())
            .andReturn();
        String attachmentId = objectMapper.readTree(attachments.getResponse().getContentAsString()).get(0).get("id").asText();

        mockMvc.perform(persona(get("/api/v1/content/{id}/attachments/{attachmentId}/download", contentId, attachmentId), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.downloadUrl").exists());
    }
}

package com.company.cms.contracts;

import com.company.cms.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AcknowledgementContractTest extends IntegrationTestBase {
    @Test
    void acknowledgementEndpointsReturnExpectedShapes() throws Exception {
        var search = mockMvc.perform(persona(get("/api/v1/content").param("q", "Annual Security"), "employee"))
            .andExpect(status().isOk())
            .andReturn();
        String contentId = objectMapper.readTree(search.getResponse().getContentAsString()).get("items").get(0).get("id").asText();

        mockMvc.perform(persona(post("/api/v1/content/{id}/acknowledgements", contentId), "employee"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.acknowledgedAt").exists());

        mockMvc.perform(persona(get("/api/v1/me/acknowledgements"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(persona(get("/api/v1/editor/acknowledgements").param("contentId", contentId), "editor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetedCount").exists());
    }
}

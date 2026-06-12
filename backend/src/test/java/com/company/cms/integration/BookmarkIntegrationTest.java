package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookmarkIntegrationTest extends IntegrationTestBase {
    @Test
    void employeeCanCreateListAndRemoveBookmarksForVisibleContent() throws Exception {
        var search = mockMvc.perform(persona(get("/api/v1/content").param("q", "Password Reset Guide"), "employee"))
            .andExpect(status().isOk())
            .andReturn();
        String contentId = objectMapper.readTree(search.getResponse().getContentAsString()).get("items").get(0).get("id").asText();

        mockMvc.perform(persona(post("/api/v1/bookmarks"), "employee")
                .content(json(Map.of("contentId", contentId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content.id").value(contentId));

        mockMvc.perform(persona(get("/api/v1/bookmarks"), "employee"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(persona(delete("/api/v1/bookmarks/{contentId}", contentId), "employee"))
            .andExpect(status().isNoContent());
    }
}

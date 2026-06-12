package com.company.cms.integration;

import com.company.cms.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportingIntegrationTest extends IntegrationTestBase {
    @Test
    void auditorCanReadAuditAndContentHealthReports() throws Exception {
        mockMvc.perform(persona(get("/api/v1/audit-events"), "auditor.cms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").exists());

        mockMvc.perform(persona(get("/api/v1/reports/content-health"), "auditor.cms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recentlyPublished").exists());
    }

    @Test
    void employeeCannotReadOperationalReports() throws Exception {
        mockMvc.perform(persona(get("/api/v1/audit-events"), "employee"))
            .andExpect(status().isForbidden());

        mockMvc.perform(persona(get("/api/v1/reports/content-health"), "employee"))
            .andExpect(status().isForbidden());
    }
}

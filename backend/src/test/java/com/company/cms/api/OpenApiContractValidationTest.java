package com.company.cms.api;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiContractValidationTest {
    @Test
    void contractFixtureIncludesCorePreviewRoutes() throws Exception {
        var resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(contract).contains("/api/v1/content");
        assertThat(contract).contains("/api/v1/bookmarks");
        assertThat(contract).contains("/api/v1/notices/pending");
        assertThat(contract).contains("/api/v1/reports/content-health");
    }
}

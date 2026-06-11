package com.company.cms.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String json(Map<String, ?> value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder persona(
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
        String user
    ) {
        return request.header("X-CMS-User", user).contentType(MediaType.APPLICATION_JSON);
    }
}

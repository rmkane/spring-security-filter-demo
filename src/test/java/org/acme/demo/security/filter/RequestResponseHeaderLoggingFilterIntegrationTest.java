package org.acme.demo.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
        "acme.security.header-filter.enabled=true",
        "logging.level.org.acme.demo.security=DEBUG"
})
class RequestResponseHeaderLoggingFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLogManualInfoRequest(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header("user-agent", "curl/8.7.1")
                        .header("accept", "application/json"))
                .andExpect(status().isOk());

        assertThat(output.getOut()).contains("Incoming request headers");
        assertThat(output.getOut()).contains("> GET /api/default/info HTTP/1.1");
        assertThat(output.getOut()).contains("Outgoing response headers");
        assertThat(output.getOut()).contains("< HTTP/1.1 200");
    }

    @Test
    void shouldNotLogIgnoredHealthCheckerRequest(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header("user-agent", "HealthChecker/1.0")
                        .header("accept", "application/json"))
                .andExpect(status().isOk());

        assertThat(output.getOut()).doesNotContain("> GET /actuator/health HTTP/1.1");
        assertThat(output.getOut()).doesNotContain("< HTTP/1.1 200");
    }

    @Test
    void shouldNotLogIgnoredWildcardGlbRequest(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header("user-agent", "GLB-Client/1.35+")
                        .header("accept", "application/json"))
                .andExpect(status().isOk());

        assertThat(output.getOut()).doesNotContain("> GET /api/default/info HTTP/1.1");
    }
}

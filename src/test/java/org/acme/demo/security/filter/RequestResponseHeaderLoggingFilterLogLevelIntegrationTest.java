package org.acme.demo.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
        "acme.security.headers.require-mtls-headers=false",
        "logging.level.org.acme.demo.security=INFO"
})
class RequestResponseHeaderLoggingFilterLogLevelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotLogWhenSecurityLogLevelIsInfo(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header("user-agent", "curl/8.7.1")
                        .header("accept", "application/json"))
                .andExpect(status().isOk());

        assertThat(output.getOut()).doesNotContain("Incoming request headers");
        assertThat(output.getOut()).doesNotContain("> GET /api/default/info HTTP/1.1");
    }
}

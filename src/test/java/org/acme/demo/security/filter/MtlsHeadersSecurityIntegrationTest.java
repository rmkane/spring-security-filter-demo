package org.acme.demo.security.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Uses the same subject DNs as {@code application.yml} allowlist and {@code scripts/lib/common.sh}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "acme.security.headers.require-mtls-headers=true",
        "acme.security.header-filter.disabled=true",
        "logging.level.org.acme.demo.security=WARN"
})
class MtlsHeadersSecurityIntegrationTest {

    private static final String SUBJECT = "x-amzn-mtls-clientcert-subject";
    private static final String ISSUER = "x-amzn-mtls-clientcert-issuer";

    /** Must match {@code acme.security.headers.allowed-client-subjects} in application.yml */
    private static final String ALLOWED_SUBJECT = "CN=demo-client,O=local";
    private static final String ALLOWED_ISSUER = "CN=demo-ca,O=local";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthShouldBeReachableWithoutMtlsHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    @Test
    void apiShouldRejectWithoutMtlsHeaders() throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header("accept", "application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiShouldAllowWithMtlsHeaders() throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header("accept", "application/json")
                        .header(SUBJECT, ALLOWED_SUBJECT)
                        .header(ISSUER, ALLOWED_ISSUER))
                .andExpect(status().isOk());
    }

    @Test
    void whoamiShouldEchoMtlsPrincipal() throws Exception {
        mockMvc.perform(get("/api/default/whoami")
                        .header(SUBJECT, ALLOWED_SUBJECT)
                        .header(ISSUER, ALLOWED_ISSUER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectDn").value(ALLOWED_SUBJECT))
                .andExpect(jsonPath("$.issuerDn").value(ALLOWED_ISSUER));
    }
}

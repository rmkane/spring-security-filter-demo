package org.acme.demo.security.filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * When {@code allowed-client-subjects} is non-empty, unknown subjects get 403 even with headers present.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "acme.security.headers.require-mtls-headers=true",
        "acme.security.headers.allowed-client-subjects[0]=CN=trusted-only,O=corp",
        "acme.security.header-filter.disabled=true",
        "logging.level.org.acme.demo.security=WARN"
})
class MtlsHeadersAllowlistIntegrationTest {

    private static final String SUBJECT = "x-amzn-mtls-clientcert-subject";
    private static final String ISSUER = "x-amzn-mtls-clientcert-issuer";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiShouldAllowAllowListedSubject() throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header(SUBJECT, "CN=trusted-only,O=corp")
                        .header(ISSUER, "CN=some-ca,O=corp"))
                .andExpect(status().isOk());
    }

    @Test
    void apiShouldRejectSubjectNotOnAllowlist() throws Exception {
        mockMvc.perform(get("/api/default/info")
                        .header(SUBJECT, "CN=demo-client,O=local")
                        .header(ISSUER, "CN=demo-ca,O=local"))
                .andExpect(status().isForbidden());
    }
}

package org.acme.demo.security.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.demo.security.config.properties.HeaderFilterProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

class RequestHeaderLoggingPolicyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldLogWhenEnabledDebugAndRequestIsNotIgnored() {
        RequestHeaderLoggingPolicy policy = new RequestHeaderLoggingPolicy(
                properties(false, "{\"user-agent\":[\"HealthChecker/*\"]}"),
                objectMapper
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "curl/8.7.1");

        assertThat(policy.shouldLog(request, true)).isTrue();
    }

    @Test
    void shouldNotLogWhenAnyConfiguredHeaderNameMatches() {
        RequestHeaderLoggingPolicy policy = new RequestHeaderLoggingPolicy(
                properties(false, "{\"user-agent\":[\"ELB-HealthChecker/*\"],\"x-probe-id\":[\"probe-123\"]}"),
                objectMapper
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("x-probe-id", "probe-123");

        assertThat(policy.shouldLog(request, true)).isFalse();
    }

    @Test
    void shouldNotLogWhenWildcardPatternMatches() {
        RequestHeaderLoggingPolicy policy = new RequestHeaderLoggingPolicy(
                properties(false, "{\"user-agent\":[\"ELB-HealthChecker/*\"]}"),
                objectMapper
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "ELB-HealthChecker/2.0");

        assertThat(policy.shouldLog(request, true)).isFalse();
    }

    @Test
    void shouldNotLogWhenDisabled() {
        RequestHeaderLoggingPolicy policy = new RequestHeaderLoggingPolicy(
                properties(true, "{\"user-agent\":[\"HealthChecker/1.0\"]}"),
                objectMapper
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "curl/8.7.1");

        assertThat(policy.shouldLog(request, true)).isFalse();
    }

    @Test
    void shouldNotLogWhenDebugIsDisabled() {
        RequestHeaderLoggingPolicy policy = new RequestHeaderLoggingPolicy(
                properties(false, "{\"user-agent\":[\"HealthChecker/1.0\"]}"),
                objectMapper
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "curl/8.7.1");

        assertThat(policy.shouldLog(request, false)).isFalse();
    }

    private HeaderFilterProperties properties(boolean disabled, String ignoreHeaders) {
        return new HeaderFilterProperties(disabled, ignoreHeaders);
    }
}

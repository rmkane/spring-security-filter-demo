package org.acme.demo.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CurlStyleHeaderLoggingUtilTest {

    @Test
    void shouldFormatRequestInCurlStyle() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.setQueryString("verbose=true");
        request.addHeader("user-agent", "curl/8.7.1");
        request.addHeader("accept", "application/json");

        String formattedRequest = CurlStyleHeaderLoggingUtil.formatRequest(request);

        assertThat(formattedRequest).isEqualTo(String.join(System.lineSeparator(),
                "> GET /api/default/info?verbose=true HTTP/1.1",
                "> user-agent: curl/8.7.1",
                "> accept: application/json"
        ));
    }

    @Test
    void shouldFormatResponseInCurlStyle() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        response.addHeader("Content-Type", "application/json");
        response.addHeader("X-Trace-Id", "abc-123");

        String formattedResponse = CurlStyleHeaderLoggingUtil.formatResponse(request, response);

        assertThat(formattedResponse).isEqualTo(String.join(System.lineSeparator(),
                "< HTTP/1.1 200",
                "< Content-Type: application/json",
                "< X-Trace-Id: abc-123"
        ));
    }

    @Test
    void shouldMaskSensitiveHeadersInRequestAndResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("Authorization", "Bearer secret-token");
        request.addHeader("Cookie", "SESSION=secret");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        response.addHeader("Set-Cookie", "SESSION=secret");
        response.addHeader("X-Api-Key", "secret-key");

        String formattedRequest = CurlStyleHeaderLoggingUtil.formatRequest(request);
        String formattedResponse = CurlStyleHeaderLoggingUtil.formatResponse(request, response);

        assertThat(formattedRequest).contains("> Authorization: ***");
        assertThat(formattedRequest).contains("> Cookie: ***");
        assertThat(formattedResponse).contains("< Set-Cookie: ***");
        assertThat(formattedResponse).contains("< X-Api-Key: ***");
    }

    @Test
    void shouldIncludeAllValuesForMultiValueHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("x-forwarded-for", "10.0.0.1");
        request.addHeader("x-forwarded-for", "10.0.0.2");

        String formattedRequest = CurlStyleHeaderLoggingUtil.formatRequest(request);

        assertThat(formattedRequest).contains("> x-forwarded-for: 10.0.0.1");
        assertThat(formattedRequest).contains("> x-forwarded-for: 10.0.0.2");
    }
}

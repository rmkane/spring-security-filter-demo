package org.acme.demo.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

class RequestResponseHeaderLoggingFilterTest {

    private Logger logger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(RequestResponseHeaderLoggingFilter.class);
        originalLevel = logger.getLevel();

        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        logger.setLevel(originalLevel);
    }

    @Test
    void shouldLogWhenRequestIsNotIgnored() throws ServletException, IOException {
        RequestResponseHeaderLoggingFilter filter = new RequestResponseHeaderLoggingFilter(
                true,
                new ObjectMapper(),
                "{\"user-agent\":[\"HealthChecker/1.0\"]}"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "curl/8.7.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(listAppender.list).hasSize(2);
        assertThat(listAppender.list.get(0).getFormattedMessage()).contains("Incoming request headers");
        assertThat(listAppender.list.get(1).getFormattedMessage()).contains("Outgoing response headers");
    }

    @Test
    void shouldNotLogWhenAnyHeaderValueMatchesIgnoredValues() throws ServletException, IOException {
        RequestResponseHeaderLoggingFilter filter = new RequestResponseHeaderLoggingFilter(
                true,
                new ObjectMapper(),
                "{\"User-Agent\":[\"HealthChecker/1.0\"]}"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.addHeader("user-agent", "curl/8.7.1");
        request.addHeader("user-agent", "HealthChecker/1.0");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldNotLogWhenFilterIsDisabled() throws ServletException, IOException {
        RequestResponseHeaderLoggingFilter filter = new RequestResponseHeaderLoggingFilter(
                false,
                new ObjectMapper(),
                "{\"user-agent\":[\"HealthChecker/1.0\"]}"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "curl/8.7.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldNotLogWhenHeaderValueMatchesWildcardPattern() throws ServletException, IOException {
        RequestResponseHeaderLoggingFilter filter = new RequestResponseHeaderLoggingFilter(
                true,
                new ObjectMapper(),
                "{\"user-agent\":[\"GLB-Client/*\"]}"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "GLB-Client/1.35+");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldNotLogWhenAnyConfiguredHeaderNameMatches() throws ServletException, IOException {
        RequestResponseHeaderLoggingFilter filter = new RequestResponseHeaderLoggingFilter(
                true,
                new ObjectMapper(),
                "{\"user-agent\":[\"GLB-Client/*\"],\"x-probe-id\":[\"probe-123\"]}"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("x-probe-id", "probe-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, new MockFilterChain());

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldLogOutgoingResponseEvenWhenChainThrows() {
        RequestResponseHeaderLoggingFilter filter = new RequestResponseHeaderLoggingFilter(
                true,
                new ObjectMapper(),
                "{\"user-agent\":[\"HealthChecker/1.0\"]}"
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/default/info");
        request.addHeader("user-agent", "curl/8.7.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, (req, res) -> {
            throw new ServletException("boom");
        })).isInstanceOf(ServletException.class)
                .hasMessage("boom");

        assertThat(listAppender.list).hasSize(2);
        assertThat(listAppender.list.get(0).getFormattedMessage()).contains("Incoming request headers");
        assertThat(listAppender.list.get(1).getFormattedMessage()).contains("Outgoing response headers");
    }
}

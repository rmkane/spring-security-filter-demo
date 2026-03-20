package org.acme.demo.security.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class HeaderFilterConfigParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldNormalizeHeaderNamesAndTrimValues() {
        String ignoredHeadersJson = """
                {
                  "User-Agent": [" ELB-HealthChecker/2.0 ", "HealthChecker/1.0"]
                }
                """;

        Map<String, Set<String>> ignoredHeaders = HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson);

        assertThat(ignoredHeaders)
                .containsEntry("user-agent", Set.of("ELB-HealthChecker/2.0", "HealthChecker/1.0"));
    }

    @Test
    void shouldAllowTrailingCommaInJson() {
        String ignoredHeadersJson = """
                {
                  "user-agent": ["ELB-HealthChecker/2.0"],
                }
                """;

        Map<String, Set<String>> ignoredHeaders = HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson);

        assertThat(ignoredHeaders)
                .containsEntry("user-agent", Set.of("ELB-HealthChecker/2.0"));
    }

    @Test
    void shouldRejectInvalidJson() {
        String ignoredHeadersJson = "{invalid-json}";

        assertThatThrownBy(() -> HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse acme.security.header-filter.ignored-headers JSON");
    }
}

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
                  "User-Agent": [" GLB-Client/1.35+ ", "HealthChecker/1.0"]
                }
                """;

        Map<String, Set<String>> ignoredHeaders = HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson);

        assertThat(ignoredHeaders)
                .containsEntry("user-agent", Set.of("GLB-Client/1.35+", "HealthChecker/1.0"));
    }

    @Test
    void shouldAllowTrailingCommaInJson() {
        String ignoredHeadersJson = """
                {
                  "user-agent": ["GLB-Client/1.35+"],
                }
                """;

        Map<String, Set<String>> ignoredHeaders = HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson);

        assertThat(ignoredHeaders)
                .containsEntry("user-agent", Set.of("GLB-Client/1.35+"));
    }

    @Test
    void shouldRejectInvalidJson() {
        String ignoredHeadersJson = "{invalid-json}";

        assertThatThrownBy(() -> HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to parse acme.security.header-filter.ignored-headers JSON");
    }
}

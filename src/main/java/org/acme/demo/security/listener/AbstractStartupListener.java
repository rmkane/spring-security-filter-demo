package org.acme.demo.security.listener;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.acme.demo.security.config.properties.HeaderFilterProperties;
import org.acme.demo.security.config.properties.HeadersProperties;
import org.acme.demo.security.utils.CollectionWritingUtils;
import org.acme.demo.security.utils.HeaderValuePatternMatcher;

// TODO: Create a ReactiveStartupListener for WebFlux
@AllArgsConstructor
@Slf4j
public abstract class AbstractStartupListener {

    private final HeaderFilterProperties headerFilterProperties;
    private final HeadersProperties headersProperties;
    private final ObjectMapper objectMapper;

    protected void logInfo(Map<String, List<HeaderValuePatternMatcher>> ignoredHeaderMatchers) {
        log.debug(
                "Headers properties: subjectDn={}, issuerDn={}, requireMtlsHeaders={}, allowedClientSubjects={}",
                headersProperties.subjectDn(),
                headersProperties.issuerDn(),
                headersProperties.requireMtlsHeaders(),
                headersProperties.allowedClientSubjects()
        );
        log.debug("Request header logging policy snapshot: disabled={}, ignoreHeaders={}",
        headerFilterProperties.disabled(),
            serializeIgnoredHeadersAsJson(ignoredHeaderMatchers)
        );
    }

    private String serializeIgnoredHeadersAsJson(Map<String, List<HeaderValuePatternMatcher>> ignoredMatchers) {
        Map<String, List<String>> stringLists = toStringListMap(ignoredMatchers);
        try {
            return objectMapper.writeValueAsString(stringLists);
        } catch (JsonProcessingException e) {
            log.error("Failed to write ignored headers to JSON", e);
            return "{}";
        }
    }

    private Map<String, List<String>> toStringListMap(Map<String, List<HeaderValuePatternMatcher>> ignoredMatchers) {
        return CollectionWritingUtils.mapToStringLists(ignoredMatchers, HeaderValuePatternMatcher::getOriginalValue);
    }

}

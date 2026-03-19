package org.acme.demo.security.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.NonNull;

import org.acme.demo.security.config.HeaderFilterProperties;
import org.acme.demo.util.HeaderFilterConfigParser;
import org.acme.demo.util.HeaderValuePatternMatcher;

@Component
public class RequestHeaderLoggingPolicy {

    private final boolean enabled;
    private final Map<String, List<HeaderValuePatternMatcher>> ignoredHeaderMatchers;

    public RequestHeaderLoggingPolicy(HeaderFilterProperties properties, ObjectMapper objectMapper) {
        this.enabled = properties.isEnabled();
        this.ignoredHeaderMatchers = compileIgnoredHeaders(
                HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, properties.getIgnoredHeaders())
        );
    }

    public boolean shouldLog(@NonNull HttpServletRequest request, boolean debugEnabled) {
        return enabled && debugEnabled && !shouldIgnore(request);
    }

    private boolean shouldIgnore(@NonNull HttpServletRequest request) {
        return ignoredHeaderMatchers.entrySet().stream()
                .anyMatch(entry -> Collections.list(request.getHeaders(entry.getKey())).stream()
                        .anyMatch(headerValue -> entry.getValue().stream()
                                .anyMatch(matcher -> matcher.matches(headerValue))));
    }

    private Map<String, List<HeaderValuePatternMatcher>> compileIgnoredHeaders(Map<String, Set<String>> ignoredHeaders) {
        if (ignoredHeaders.isEmpty()) {
            return Map.of();
        }

        Map<String, List<HeaderValuePatternMatcher>> compiledMatchers = new LinkedHashMap<>();

        ignoredHeaders.forEach((headerName, ignoredValues) -> compiledMatchers.put(
                headerName,
                ignoredValues.stream()
                        .map(HeaderValuePatternMatcher::compile)
                        .toList()
        ));

        return Map.copyOf(compiledMatchers);
    }
}

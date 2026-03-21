package org.acme.demo.security.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.NonNull;

import org.acme.demo.security.config.properties.HeaderFilterProperties;
import org.acme.demo.security.utils.HeaderFilterConfigParser;
import org.acme.demo.security.utils.HeaderValuePatternMatcher;

@Component
@Getter
public class RequestHeaderLoggingPolicy {

    private final boolean disabled;
    private final Map<String, List<HeaderValuePatternMatcher>> ignoredHeaderMatchers;

    public RequestHeaderLoggingPolicy(HeaderFilterProperties properties, ObjectMapper objectMapper) {
        this.disabled = properties.disabled();
        this.ignoredHeaderMatchers = compileIgnoredHeaders(
                HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, properties.ignoreHeaders())
        );
    }

    public boolean shouldLog(@NonNull HttpServletRequest request, boolean debugEnabled) {
        return !disabled && debugEnabled && !shouldIgnore(request);
    }

    /**
     * Whether the request matches {@code acme.security.header-filter.ignore-headers} (e.g. probe {@code User-Agent}
     * patterns). Independent of whether the request/response logging filter is enabled—useful to suppress other DEBUG
     * noise for the same traffic.
     */
    public boolean matchesIgnoreRules(@NonNull HttpServletRequest request) {
        return shouldIgnore(request);
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

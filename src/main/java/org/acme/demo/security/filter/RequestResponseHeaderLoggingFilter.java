package org.acme.demo.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.lang.NonNull;

import org.acme.demo.util.CurlStyleHeaderLoggingUtil;
import org.acme.demo.util.HeaderFilterConfigParser;

@Component
@Slf4j
public class RequestResponseHeaderLoggingFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final Map<String, Set<String>> ignoredHeaders;

    public RequestResponseHeaderLoggingFilter(
            @Value("${acme.security.header-filter.enabled:true}")
            boolean enabled,
            ObjectMapper objectMapper,
            @Value("${acme.security.header-filter.ignored-headers:}")
            String ignoredHeadersJson
    ) {
        this.enabled = enabled;
        this.ignoredHeaders = HeaderFilterConfigParser.parseIgnoredHeaders(objectMapper, ignoredHeadersJson);
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!shouldLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Incoming request headers\n{}", CurlStyleHeaderLoggingUtil.formatRequest(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            log.debug("Outgoing response headers\n{}", CurlStyleHeaderLoggingUtil.formatResponse(request, response));
        }
    }

    private boolean shouldLog(@NonNull HttpServletRequest request) {
        return enabled && log.isDebugEnabled() && !shouldIgnore(request);
    }

    private boolean shouldIgnore(@NonNull HttpServletRequest request) {
        return ignoredHeaders.entrySet().stream()
            .anyMatch(entry -> Collections.list(request.getHeaders(entry.getKey())).stream()
                .anyMatch(headerValue -> entry.getValue().stream()
                    .anyMatch(ignoredValuePattern -> matchesIgnoredPattern(headerValue, ignoredValuePattern))));
    }

    private boolean matchesIgnoredPattern(String headerValue, String ignoredValuePattern) {
        if (!ignoredValuePattern.contains("*")) {
            return ignoredValuePattern.equals(headerValue);
        }

        String regex = Arrays.stream(ignoredValuePattern.split("\\*", -1))
                .map(Pattern::quote)
                .collect(Collectors.joining(".*"));
        return Pattern.compile(regex).matcher(headerValue).matches();
    }
}

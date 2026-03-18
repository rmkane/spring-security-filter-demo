package org.acme.demo.security.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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

    private final Map<String, Set<String>> ignoredHeaders;

    public RequestResponseHeaderLoggingFilter(
            ObjectMapper objectMapper,
            @Value("${acme.security.header-filter.ignored-headers:}")
            String ignoredHeadersJson
    ) {
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
        
        filterChain.doFilter(request, response);

        log.debug("Outgoing response headers\n{}", CurlStyleHeaderLoggingUtil.formatResponse(request, response));
    }

    private boolean shouldLog(@NonNull HttpServletRequest request) {
        return log.isDebugEnabled()
            && isHelloRequest(request)
            && !shouldIgnore(request);
    }

    private boolean isHelloRequest(@NonNull HttpServletRequest request) {
        return "/api/default/hello".equals(request.getRequestURI());
    }

    private boolean shouldIgnore(@NonNull HttpServletRequest request) {
        return ignoredHeaders.entrySet().stream()
            .anyMatch(entry -> request.getHeader(entry.getKey()) != null && entry.getValue().contains(request.getHeader(entry.getKey())));
    }
}

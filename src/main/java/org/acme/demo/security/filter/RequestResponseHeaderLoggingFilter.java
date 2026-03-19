package org.acme.demo.security.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.common.lang.NonNull;

import org.acme.demo.security.utils.CurlStyleHeaderLoggingUtil;

@Component
@Slf4j
public class RequestResponseHeaderLoggingFilter extends OncePerRequestFilter {

    private final RequestHeaderLoggingPolicy requestHeaderLoggingPolicy;

    public RequestResponseHeaderLoggingFilter(RequestHeaderLoggingPolicy requestHeaderLoggingPolicy) {
        this.requestHeaderLoggingPolicy = requestHeaderLoggingPolicy;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!requestHeaderLoggingPolicy.shouldLog(request, log.isDebugEnabled())) {
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
}

package org.acme.demo.security.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.common.lang.NonNull;

import org.acme.demo.security.authentication.MtlsClientPrincipal;
import org.acme.demo.security.config.SecurityConfig;
import org.acme.demo.security.config.properties.HeadersProperties;

/**
 * Populates the {@link SecurityContext} from mTLS certificate headers forwarded by the load balancer.
 * Health endpoints are skipped so probes work without client cert headers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MtlsHeadersAuthenticationFilter extends OncePerRequestFilter {

    private final HeadersProperties headersProperties;
    private final RequestHeaderLoggingPolicy requestHeaderLoggingPolicy;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!headersProperties.requireMtlsHeaders()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isHealthEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String subject = firstHeader(request, headersProperties.subjectDn());
        String issuer = firstHeader(request, headersProperties.issuerDn());

        if (subject == null || subject.isBlank() || issuer == null || issuer.isBlank()) {
            log.debug("Rejecting request without mTLS forwarding headers: uri={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing mTLS client certificate headers");
            return;
        }

        String subjectTrimmed = subject.trim();
        String issuerTrimmed = issuer.trim();

        if (!isSubjectAllowListed(subjectTrimmed)) {
            log.debug("Rejecting request with subject not in allowlist: uri={}, subject={}", request.getRequestURI(), subjectTrimmed);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Client certificate subject is not authorized");
            return;
        }

        MtlsClientPrincipal principal = new MtlsClientPrincipal(subjectTrimmed, issuerTrimmed);
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                principal,
                "N/A",
                AuthorityUtils.createAuthorityList("ROLE_" + SecurityConfig.ROLE_MTLS_CLIENT)
        );
        authentication.setAuthenticated(true);
        authentication.setDetails(request);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        if (log.isDebugEnabled() && !requestHeaderLoggingPolicy.matchesIgnoreRules(request)) {
            log.debug("Authenticated request as mTLS client subject={}", principal.getName());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * When the allowlist is empty, any non-blank subject is accepted (headers-only gate).
     * When non-empty, the subject DN must match an entry exactly (after trim).
     */
    private boolean isSubjectAllowListed(String subjectTrimmed) {
        List<String> allowed = headersProperties.allowedClientSubjects();
        if (allowed.isEmpty()) {
            return true;
        }
        return allowed.stream().map(String::trim).anyMatch(subjectTrimmed::equals);
    }

    private static boolean isHealthEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/actuator/health")
                || path.startsWith("/actuator/health/");
    }

    /**
     * Servlet container may expose multiple values; use the first non-blank.
     */
    private static String firstHeader(HttpServletRequest request, String name) {
        Enumeration<String> values = request.getHeaders(name);
        if (values == null) {
            return null;
        }
        for (String value : Collections.list(values)) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

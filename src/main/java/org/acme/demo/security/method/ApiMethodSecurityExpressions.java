package org.acme.demo.security.method;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import org.acme.demo.security.config.properties.HeadersProperties;

/**
 * Named bean for {@code @PreAuthorize} SpEL (e.g. {@code @apiMethodSecurityExpressions}).
 */
@Component("apiMethodSecurityExpressions")
@RequiredArgsConstructor
public class ApiMethodSecurityExpressions {

    private final HeadersProperties headersProperties;

    /** When {@code true}, mTLS header enforcement is off and API methods may run without {@code ROLE_MTLS_CLIENT}. */
    public boolean isMtlsEnforcementDisabled() {
        return !headersProperties.requireMtlsHeaders();
    }
}

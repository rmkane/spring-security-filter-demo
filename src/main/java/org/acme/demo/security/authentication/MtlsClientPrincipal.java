package org.acme.demo.security.authentication;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;

/**
 * Client identity derived from reverse-proxy mTLS forwarding headers (e.g. AWS ALB).
 */
public record MtlsClientPrincipal(String subjectDn, String issuerDn) implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return subjectDn;
    }
}

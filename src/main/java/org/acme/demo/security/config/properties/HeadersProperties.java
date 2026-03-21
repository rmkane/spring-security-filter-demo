package org.acme.demo.security.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "acme.security.headers")
public record HeadersProperties(
        @DefaultValue("x-amzn-mtls-clientcert-subject") String subjectDn,
        @DefaultValue("x-amzn-mtls-clientcert-issuer") String issuerDn,
        /**
         * When {@code true}, non-health requests must carry mTLS forwarding headers and (if configured) an allow-listed subject.
         */
        @DefaultValue("true") boolean requireMtlsHeaders,
        /**
         * Permitted client certificate subject DNs (exact match after trim). Acts as an in-memory allowlist of logical clients.
         * When empty, any non-blank subject + issuer pair is accepted once headers are present.
         */
        List<String> allowedClientSubjects
) {

    public HeadersProperties {
        allowedClientSubjects = allowedClientSubjects == null ? List.of() : List.copyOf(allowedClientSubjects);
    }
}

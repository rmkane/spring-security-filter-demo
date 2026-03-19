package org.acme.demo.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "acme.security.header-filter")
public record HeaderFilterProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("") String ignoredHeaders
) {
}

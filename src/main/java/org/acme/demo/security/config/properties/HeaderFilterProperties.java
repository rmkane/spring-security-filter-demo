package org.acme.demo.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "acme.security.header-filter")
public record HeaderFilterProperties(
        @DefaultValue("false") boolean disabled,
        @DefaultValue("{}") String ignoreHeaders
) {
}

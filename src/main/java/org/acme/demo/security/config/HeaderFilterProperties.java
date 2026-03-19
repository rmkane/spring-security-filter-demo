package org.acme.demo.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "acme.security.header-filter")
public class HeaderFilterProperties {

    private boolean enabled = true;
    private String ignoredHeaders = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getIgnoredHeaders() {
        return ignoredHeaders;
    }

    public void setIgnoredHeaders(String ignoredHeaders) {
        this.ignoredHeaders = ignoredHeaders;
    }
}

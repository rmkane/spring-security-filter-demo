package org.acme.demo.security.listener;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.acme.demo.security.config.properties.HeaderFilterProperties;
import org.acme.demo.security.config.properties.HeadersProperties;
import org.acme.demo.security.filter.RequestHeaderLoggingPolicy;

@Component
@Slf4j
public class StartupListener extends AbstractStartupListener {

    // MVC-specific
    private final RequestHeaderLoggingPolicy requestHeaderLoggingPolicy;

    public StartupListener(
        HeaderFilterProperties headerFilterProperties,
        HeadersProperties headersProperties,
        ObjectMapper objectMapper,
        RequestHeaderLoggingPolicy requestHeaderLoggingPolicy
    ) {
        super(headerFilterProperties, headersProperties, objectMapper);
        this.requestHeaderLoggingPolicy = requestHeaderLoggingPolicy;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logPolicySnapshotAtStartup() {
        logInfo(requestHeaderLoggingPolicy.getIgnoredHeaderMatchers());
    }    
}

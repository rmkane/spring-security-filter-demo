package org.acme.demo.security.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import org.acme.demo.security.filter.RequestHeaderLoggingPolicy;
import org.acme.demo.security.utils.HeaderValuePatternMatcher;
import org.acme.demo.security.utils.CollectionWritingUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestHeaderLoggingPolicyStartupListener {

    private final RequestHeaderLoggingPolicy requestHeaderLoggingPolicy;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void logPolicySnapshotAtStartup() {

        log.debug("Request header logging policy snapshot: enabled={}, ignoredHeaders={}",
            requestHeaderLoggingPolicy.isEnabled(),
            serializeIgnoredHeadersAsJson(requestHeaderLoggingPolicy.getIgnoredHeaderMatchers())
        );
    }

    private String serializeIgnoredHeadersAsJson(Map<String, List<HeaderValuePatternMatcher>> ignoredMatchers) {
        Map<String, List<String>> stringLists = toStringListMap(ignoredMatchers);
        try {
            return objectMapper.writeValueAsString(stringLists);
        } catch (JsonProcessingException e) {
            log.error("Failed to write ignored headers to JSON", e);
            return "{}";
        }
    }

    private Map<String, List<String>> toStringListMap(Map<String, List<HeaderValuePatternMatcher>> ignoredMatchers) {
        return CollectionWritingUtils.mapToStringLists(ignoredMatchers, HeaderValuePatternMatcher::getOriginalValue);
    }
}

package org.acme.demo.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.json.JsonReadFeature;

public final class HeaderFilterConfigParser {

    private static final TypeReference<Map<String, Set<String>>> IGNORED_HEADERS_TYPE = new TypeReference<>() {
    };

    private HeaderFilterConfigParser() {
    }

    public static Map<String, Set<String>> parseIgnoredHeaders(ObjectMapper objectMapper, String ignoredHeadersJson) {
        if (ignoredHeadersJson == null || ignoredHeadersJson.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.copy()
                    .configure(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature(), true)
                    .readValue(ignoredHeadersJson, IGNORED_HEADERS_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Failed to parse acme.security.header-filter.ignored-headers JSON", exception);
        }
    }
}

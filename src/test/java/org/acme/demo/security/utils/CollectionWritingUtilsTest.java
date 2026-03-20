package org.acme.demo.security.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CollectionWritingUtilsTest {

    @Test
    void shouldConvertMatcherMapToStringListMap() {
        Map<String, List<HeaderValuePatternMatcher>> ignoredMatchers = Map.of(
                "user-agent", List.of(
                        HeaderValuePatternMatcher.compile("ELB-HealthChecker/*"),
                        HeaderValuePatternMatcher.compile("HealthChecker/*")
                )
        );

        Map<String, List<String>> converted = CollectionWritingUtils.mapToStringLists(
                ignoredMatchers,
                HeaderValuePatternMatcher::getOriginalValue
        );

        assertThat(converted).containsEntry("user-agent", List.of("ELB-HealthChecker/*", "HealthChecker/*"));
    }

    @Test
    void shouldReturnEmptyMapForNullOrEmptyInput() {
        assertThat(CollectionWritingUtils.mapToStringLists(Map.of(), String::valueOf)).isEmpty();
        assertThat(CollectionWritingUtils.mapToStringLists(null, String::valueOf)).isEmpty();
    }
}

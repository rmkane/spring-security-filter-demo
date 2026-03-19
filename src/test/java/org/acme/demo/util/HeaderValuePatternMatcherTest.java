package org.acme.demo.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HeaderValuePatternMatcherTest {

    @Test
    void shouldMatchExactValue() {
        HeaderValuePatternMatcher matcher = HeaderValuePatternMatcher.compile("HealthChecker/1.0");

        assertThat(matcher.matches("HealthChecker/1.0")).isTrue();
        assertThat(matcher.matches("HealthChecker/2.0")).isFalse();
    }

    @Test
    void shouldMatchWildcardValue() {
        HeaderValuePatternMatcher matcher = HeaderValuePatternMatcher.compile("GLB-Client/*");

        assertThat(matcher.matches("GLB-Client/1.35+")).isTrue();
        assertThat(matcher.matches("GLB-Client/region-a")).isTrue();
        assertThat(matcher.matches("Other-Client/1.35+")).isFalse();
    }

    @Test
    void shouldMatchWildcardInMiddleOfPattern() {
        HeaderValuePatternMatcher matcher = HeaderValuePatternMatcher.compile("*Checker*");

        assertThat(matcher.matches("HealthChecker/1.0")).isTrue();
        assertThat(matcher.matches("Checker")).isTrue();
        assertThat(matcher.matches("GLB-Client/1.35+")).isFalse();
    }
}

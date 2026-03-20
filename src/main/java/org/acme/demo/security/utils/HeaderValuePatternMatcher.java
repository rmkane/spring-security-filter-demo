package org.acme.demo.security.utils;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class HeaderValuePatternMatcher {

    private final String exactValue;
    private final Pattern compiledPattern;

    public static HeaderValuePatternMatcher compile(String ignoredValuePattern) {
        if (!ignoredValuePattern.contains("*")) {
            return new HeaderValuePatternMatcher(ignoredValuePattern, null);
        }

        String regex = Arrays.stream(ignoredValuePattern.split("\\*", -1))
                .map(Pattern::quote)
                .collect(Collectors.joining(".*"));

        return new HeaderValuePatternMatcher(null, Pattern.compile(regex));
    }

    public boolean matches(String headerValue) {
        if (compiledPattern == null) {
            return exactValue.equals(headerValue);
        }

        return compiledPattern.matcher(headerValue).matches();
    }
}

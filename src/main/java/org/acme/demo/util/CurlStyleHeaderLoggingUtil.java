package org.acme.demo.util;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class CurlStyleHeaderLoggingUtil {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "x-api-key"
    );

    private CurlStyleHeaderLoggingUtil() {
    }

    public static String formatRequest(HttpServletRequest request) {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        String requestTarget = request.getRequestURI();

        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            requestTarget += "?" + request.getQueryString();
        }

        joiner.add("> " + request.getMethod() + " " + requestTarget + " " + request.getProtocol());

        for (String headerName : Collections.list(request.getHeaderNames())) {
            for (String headerValue : Collections.list(request.getHeaders(headerName))) {
                joiner.add("> " + headerName + ": " + sanitizeHeaderValue(headerName, headerValue));
            }
        }

        return joiner.toString();
    }

    public static String formatResponse(HttpServletRequest request, HttpServletResponse response) {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("< " + request.getProtocol() + " " + response.getStatus());

        for (String headerName : response.getHeaderNames()) {
            for (String headerValue : response.getHeaders(headerName)) {
                joiner.add("< " + headerName + ": " + sanitizeHeaderValue(headerName, headerValue));
            }
        }

        return joiner.toString();
    }

    private static String sanitizeHeaderValue(String headerName, String headerValue) {
        if (SENSITIVE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT))) {
            return "***";
        }

        return headerValue;
    }
}

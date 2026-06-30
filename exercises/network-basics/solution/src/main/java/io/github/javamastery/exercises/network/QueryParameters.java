package io.github.javamastery.exercises.network;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryParameters {
    public Map<String, List<String>> parse(String query) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return parameters;
        }

        String normalized = query.startsWith("?") ? query.substring(1) : query;
        if (normalized.isEmpty()) {
            return parameters;
        }

        for (String pair : normalized.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String rawName = separator >= 0 ? pair.substring(0, separator) : pair;
            String rawValue = separator >= 0 ? pair.substring(separator + 1) : "";

            parameters
                    .computeIfAbsent(decode(rawName), ignored -> new ArrayList<>())
                    .add(decode(rawValue));
        }

        return parameters;
    }

    public String firstValue(Map<String, List<String>> parameters, String name) {
        List<String> values = parameters.get(name);
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.get(0);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

package io.github.javamastery.exercises.os;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EnvironmentVariables {
    public String required(Map<String, String> environment, String name) {
        String value = valueOf(environment, name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("missing environment variable: " + name);
        }
        return value;
    }

    public String withDefault(Map<String, String> environment, String name, String fallback) {
        String value = valueOf(environment, name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    public List<String> pathEntries(Map<String, String> environment, String separator) {
        if (separator == null || separator.isEmpty()) {
            throw new IllegalArgumentException("separator must not be blank");
        }
        String path = withDefault(environment, "PATH", "");
        List<String> entries = new ArrayList<>();
        if (path.isBlank()) {
            return entries;
        }

        for (String entry : path.split(Pattern.quote(separator))) {
            if (!entry.isBlank()) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private String valueOf(Map<String, String> environment, String name) {
        if (environment == null) {
            throw new IllegalArgumentException("environment must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return environment.get(name);
    }
}

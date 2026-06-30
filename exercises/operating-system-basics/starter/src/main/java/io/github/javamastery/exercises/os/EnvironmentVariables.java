package io.github.javamastery.exercises.os;

import java.util.List;
import java.util.Map;

public class EnvironmentVariables {
    public String required(Map<String, String> environment, String name) {
        return "";
    }

    public String withDefault(Map<String, String> environment, String name, String fallback) {
        return fallback;
    }

    public List<String> pathEntries(Map<String, String> environment, String separator) {
        return List.of();
    }
}

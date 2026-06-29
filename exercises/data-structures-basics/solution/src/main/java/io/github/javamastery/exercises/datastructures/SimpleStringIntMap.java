package io.github.javamastery.exercises.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public class SimpleStringIntMap {
    private final Map<String, Integer> values = new LinkedHashMap<>();

    public OptionalInt put(String key, int value) {
        Integer previous = values.put(requireKey(key), value);
        return previous == null ? OptionalInt.empty() : OptionalInt.of(previous);
    }

    public OptionalInt get(String key) {
        Integer value = values.get(requireKey(key));
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    public OptionalInt remove(String key) {
        Integer removed = values.remove(requireKey(key));
        return removed == null ? OptionalInt.empty() : OptionalInt.of(removed);
    }

    public boolean containsKey(String key) {
        return values.containsKey(requireKey(key));
    }

    public int size() {
        return values.size();
    }

    public Set<String> keys() {
        return Set.copyOf(values.keySet());
    }

    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        return key.strip();
    }
}

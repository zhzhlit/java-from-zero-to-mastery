package io.github.javamastery.basics.controlflow;

public final class NumberSummation {

    private NumberSummation() {
    }

    public static long sumTo(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative");
        }
        long total = 0;
        for (int current = 1; current <= limit; current++) {
            total += current;
        }
        return total;
    }
}

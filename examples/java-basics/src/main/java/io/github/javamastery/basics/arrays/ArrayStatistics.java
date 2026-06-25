package io.github.javamastery.basics.arrays;

public final class ArrayStatistics {

    private ArrayStatistics() {
    }

    public static int sum(int[] values) {
        requireArray(values);
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    public static double average(int[] values) {
        requireNonEmpty(values);
        return (double) sum(values) / values.length;
    }

    public static int maximum(int[] values) {
        requireNonEmpty(values);
        int maximum = values[0];
        for (int index = 1; index < values.length; index++) {
            if (values[index] > maximum) {
                maximum = values[index];
            }
        }
        return maximum;
    }

    private static void requireArray(int[] values) {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
    }

    private static void requireNonEmpty(int[] values) {
        requireArray(values);
        if (values.length == 0) {
            throw new IllegalArgumentException("values must not be empty");
        }
    }
}

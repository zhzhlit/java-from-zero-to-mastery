package io.github.javamastery.basics.arrays;

import java.util.Arrays;

public final class ArrayCopying {

    private ArrayCopying() {
    }

    public static int[] sortedCopy(int[] source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        int[] copy = Arrays.copyOf(source, source.length);
        Arrays.sort(copy);
        return copy;
    }
}

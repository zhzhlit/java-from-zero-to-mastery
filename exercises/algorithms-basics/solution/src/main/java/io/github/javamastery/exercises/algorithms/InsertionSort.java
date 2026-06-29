package io.github.javamastery.exercises.algorithms;

import java.util.Arrays;

public class InsertionSort {
    public int[] sort(int[] values) {
        int[] sorted = Arrays.copyOf(values, values.length);
        for (int index = 1; index < sorted.length; index++) {
            int value = sorted[index];
            int position = index - 1;

            while (position >= 0 && sorted[position] > value) {
                sorted[position + 1] = sorted[position];
                position--;
            }

            sorted[position + 1] = value;
        }
        return sorted;
    }
}

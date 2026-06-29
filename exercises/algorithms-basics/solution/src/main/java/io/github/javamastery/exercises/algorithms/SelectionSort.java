package io.github.javamastery.exercises.algorithms;

import java.util.Arrays;

public class SelectionSort {
    public int[] sort(int[] values) {
        int[] sorted = Arrays.copyOf(values, values.length);
        for (int current = 0; current < sorted.length - 1; current++) {
            int minIndex = current;
            for (int candidate = current + 1; candidate < sorted.length; candidate++) {
                if (sorted[candidate] < sorted[minIndex]) {
                    minIndex = candidate;
                }
            }
            swap(sorted, current, minIndex);
        }
        return sorted;
    }

    private void swap(int[] values, int left, int right) {
        int temp = values[left];
        values[left] = values[right];
        values[right] = temp;
    }
}

package io.github.javamastery.exercises.algorithms;

public class LinearSearch {
    public int findFirst(int[] values, int target) {
        for (int index = 0; index < values.length; index++) {
            if (values[index] == target) {
                return index;
            }
        }
        return -1;
    }
}

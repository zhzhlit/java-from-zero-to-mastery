package io.github.javamastery.exercises.algorithms;

public class BinarySearch {
    public int find(int[] sortedValues, int target) {
        int left = 0;
        int right = sortedValues.length - 1;

        while (left <= right) {
            int middle = left + (right - left) / 2;
            if (sortedValues[middle] == target) {
                return middle;
            }
            if (sortedValues[middle] < target) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }

        return -1;
    }
}

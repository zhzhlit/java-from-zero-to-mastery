package io.github.javamastery.exercises.algorithms;

public class ComplexityExplorer {
    public int constantSteps(int size) {
        requireNonNegative(size);
        return 1;
    }

    public int linearSteps(int size) {
        requireNonNegative(size);
        int steps = 0;
        for (int index = 0; index < size; index++) {
            steps++;
        }
        return steps;
    }

    public int quadraticSteps(int size) {
        requireNonNegative(size);
        int steps = 0;
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                steps++;
            }
        }
        return steps;
    }

    private void requireNonNegative(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
    }
}

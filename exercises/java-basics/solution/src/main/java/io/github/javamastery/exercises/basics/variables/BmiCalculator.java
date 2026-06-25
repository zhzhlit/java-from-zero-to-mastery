package io.github.javamastery.exercises.basics.variables;

public final class BmiCalculator {

    private BmiCalculator() {
    }

    public static double calculate(double weightKg, double heightMeters) {
        if (weightKg <= 0 || heightMeters <= 0) {
            throw new IllegalArgumentException("weight and height must be positive");
        }
        return weightKg / (heightMeters * heightMeters);
    }
}

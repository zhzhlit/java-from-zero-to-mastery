package io.github.javamastery.exercises;

public final class NumberClassifier {

    private NumberClassifier() {
    }

    public static String classify(int number) {
        if (number > 0) {
            return "positive";
        }
        if (number < 0) {
            return "negative";
        }
        return "zero";
    }
}

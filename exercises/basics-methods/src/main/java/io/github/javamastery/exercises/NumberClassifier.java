package io.github.javamastery.exercises;

public final class NumberClassifier {

    private NumberClassifier() {
    }

    static String classify(int number) {
        if (number > 0) {
            return "positive";
        }
        if (number < 0) {
            return "negative";
        }
        return "zero";
    }
}

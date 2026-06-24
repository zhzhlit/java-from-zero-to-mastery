package io.github.javamastery.basics.controlflow;

public final class GradeClassifier {

    private GradeClassifier() {
    }

    public static String classify(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        return switch (score / 10) {
            case 10, 9 -> "A";
            case 8 -> "B";
            case 7 -> "C";
            case 6 -> "D";
            default -> "F";
        };
    }
}

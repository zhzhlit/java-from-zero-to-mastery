package io.github.javamastery.exercises.basics.statistics;

public final class ScoreParser {

    private ScoreParser() {
    }

    public static int[] parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("score text must not be blank");
        }

        String[] parts = text.split(",", -1);
        int[] scores = new int[parts.length];
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index].strip();
            if (part.isEmpty()) {
                throw new IllegalArgumentException("score must not be missing");
            }
            try {
                scores[index] = Integer.parseInt(part);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(
                        "score must be an integer: " + part, exception);
            }
            if (scores[index] < 0 || scores[index] > 100) {
                throw new IllegalArgumentException(
                        "score must be between 0 and 100: " + scores[index]);
            }
        }
        return scores;
    }
}

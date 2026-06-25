package io.github.javamastery.exercises.basics.statistics;

public final class ScoreStatistics {

    private ScoreStatistics() {
    }

    public static int total(int[] scores) {
        requireScores(scores);
        int total = 0;
        for (int score : scores) {
            total += score;
        }
        return total;
    }

    public static double average(int[] scores) {
        requireScores(scores);
        return (double) total(scores) / scores.length;
    }

    public static int highest(int[] scores) {
        requireScores(scores);
        int highest = scores[0];
        for (int score : scores) {
            if (score > highest) {
                highest = score;
            }
        }
        return highest;
    }

    public static int lowest(int[] scores) {
        requireScores(scores);
        int lowest = scores[0];
        for (int score : scores) {
            if (score < lowest) {
                lowest = score;
            }
        }
        return lowest;
    }

    public static int countAtLeast(int[] scores, int threshold) {
        requireScores(scores);
        if (threshold < 0 || threshold > 100) {
            throw new IllegalArgumentException(
                    "threshold must be between 0 and 100");
        }
        int count = 0;
        for (int score : scores) {
            if (score >= threshold) {
                count++;
            }
        }
        return count;
    }

    private static void requireScores(int[] scores) {
        if (scores == null || scores.length == 0) {
            throw new IllegalArgumentException("scores must not be empty");
        }
    }
}

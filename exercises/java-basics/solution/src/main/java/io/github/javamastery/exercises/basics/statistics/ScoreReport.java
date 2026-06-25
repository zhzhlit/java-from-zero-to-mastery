package io.github.javamastery.exercises.basics.statistics;

import java.util.Locale;

public final class ScoreReport {

    private ScoreReport() {
    }

    public static String build(String scoreText, int passScore) {
        int[] scores = ScoreParser.parse(scoreText);
        String average = String.format(
                Locale.ROOT, "%.2f", ScoreStatistics.average(scores));

        return new StringBuilder()
                .append("Count: ").append(scores.length).append('\n')
                .append("Total: ").append(ScoreStatistics.total(scores)).append('\n')
                .append("Average: ").append(average).append('\n')
                .append("Highest: ").append(ScoreStatistics.highest(scores)).append('\n')
                .append("Lowest: ").append(ScoreStatistics.lowest(scores)).append('\n')
                .append("Passed: ")
                .append(ScoreStatistics.countAtLeast(scores, passScore))
                .toString();
    }
}

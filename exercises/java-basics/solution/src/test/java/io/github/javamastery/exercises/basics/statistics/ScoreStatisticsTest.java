package io.github.javamastery.exercises.basics.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreStatisticsTest {

    private final int[] scores = {88, 92, 76, 100};

    @Test
    void calculatesAllStatistics() {
        assertEquals(356, ScoreStatistics.total(scores));
        assertEquals(89.0, ScoreStatistics.average(scores), 0.0001);
        assertEquals(100, ScoreStatistics.highest(scores));
        assertEquals(76, ScoreStatistics.lowest(scores));
        assertEquals(3, ScoreStatistics.countAtLeast(scores, 80));
    }

    @Test
    void rejectsNullOrEmptyScoresAndInvalidThreshold() {
        assertThrows(IllegalArgumentException.class,
                () -> ScoreStatistics.total(null));
        assertThrows(IllegalArgumentException.class,
                () -> ScoreStatistics.average(new int[]{}));
        assertThrows(IllegalArgumentException.class,
                () -> ScoreStatistics.countAtLeast(scores, 101));
    }
}

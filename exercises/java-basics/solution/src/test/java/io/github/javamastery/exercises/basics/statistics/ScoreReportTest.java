package io.github.javamastery.exercises.basics.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreReportTest {

    @Test
    void buildsDeterministicReport() {
        assertEquals("""
                Count: 4
                Total: 356
                Average: 89.00
                Highest: 100
                Lowest: 76
                Passed: 3""",
                ScoreReport.build("88, 92, 76, 100", 80));
    }
}

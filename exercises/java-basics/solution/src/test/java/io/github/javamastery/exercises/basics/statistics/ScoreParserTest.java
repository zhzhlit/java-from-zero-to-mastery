package io.github.javamastery.exercises.basics.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreParserTest {

    @Test
    void parsesCommaSeparatedScoresAndWhitespace() {
        assertArrayEquals(new int[]{88, 92, 76, 100},
                ScoreParser.parse("88, 92,76, 100"));
    }

    @Test
    void acceptsBoundaryScores() {
        assertArrayEquals(new int[]{0, 100}, ScoreParser.parse("0,100"));
    }

    @Test
    void rejectsNullEmptyMissingNonNumericAndOutOfRangeInput() {
        assertThrows(IllegalArgumentException.class, () -> ScoreParser.parse(null));
        assertThrows(IllegalArgumentException.class, () -> ScoreParser.parse("  "));
        assertThrows(IllegalArgumentException.class, () -> ScoreParser.parse("80,,90"));
        assertThrows(IllegalArgumentException.class, () -> ScoreParser.parse("80,A"));
        assertThrows(IllegalArgumentException.class, () -> ScoreParser.parse("101"));
        assertThrows(IllegalArgumentException.class, () -> ScoreParser.parse("-1"));
    }
}

package io.github.javamastery.basics.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportBuilderTest {

    @Test
    void buildsNumberedLinesWithStringBuilder() {
        assertEquals(
                "1. arrays\n2. strings\n3. debugging",
                ReportBuilder.numberedLines(new String[]{"arrays", "strings", "debugging"})
        );
    }

    @Test
    void returnsEmptyTextForEmptyArray() {
        assertEquals("", ReportBuilder.numberedLines(new String[]{}));
    }

    @Test
    void rejectsNullItems() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportBuilder.numberedLines(null));
    }
}

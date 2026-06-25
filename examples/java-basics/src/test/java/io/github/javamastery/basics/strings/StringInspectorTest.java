package io.github.javamastery.basics.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringInspectorTest {

    @Test
    void normalizesOuterAndRepeatedWhitespace() {
        assertEquals("Java 21 is ready",
                StringInspector.normalizeWhitespace("  Java   21 is   ready  "));
    }

    @Test
    void comparesTextWithoutUsingReferenceIdentity() {
        assertTrue(StringInspector.sameText(new String("Java"), "Java"));
        assertFalse(StringInspector.sameText("Java", "java"));
    }

    @Test
    void rejectsNullText() {
        assertThrows(IllegalArgumentException.class,
                () -> StringInspector.normalizeWhitespace(null));
    }
}

package io.github.javamastery.basics.methods;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFormatterTest {

    @Test
    void formatsWithDefaultSeparator() {
        assertEquals("Java: 21", TextFormatter.label("Java", "21"));
    }

    @Test
    void formatsWithCustomSeparator() {
        assertEquals("Java -> 21", TextFormatter.label("Java", "21", " -> "));
    }
}

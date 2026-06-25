package io.github.javamastery.basics.debugging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebuggingScenariosTest {

    @Test
    void findsLastArrayElementWithoutRunningPastTheBoundary() {
        assertEquals(2,
                DebuggingScenarios.findFirstIndex(new int[]{4, 7, 9}, 9));
    }

    @Test
    void returnsMinusOneWhenValueIsMissing() {
        assertEquals(-1,
                DebuggingScenarios.findFirstIndex(new int[]{4, 7, 9}, 5));
    }

    @Test
    void comparesNamesByContent() {
        assertTrue(DebuggingScenarios.containsName(
                new String[]{"Ada", new String("Linus")}, "Linus"));
        assertFalse(DebuggingScenarios.containsName(
                new String[]{"Ada", "Linus"}, "Grace"));
    }
}

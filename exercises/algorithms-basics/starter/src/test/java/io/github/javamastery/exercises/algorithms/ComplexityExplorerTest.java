package io.github.javamastery.exercises.algorithms;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ComplexityExplorer")
class ComplexityExplorerTest {
    private final ComplexityExplorer explorer = new ComplexityExplorer();

    @Test
    @Disabled("练习：移除 @Disabled，理解 O(1)")
    @DisplayName("常数次数不随输入规模增长")
    void constantStepsDoNotGrowWithSize() {
        assertEquals(1, explorer.constantSteps(0));
        assertEquals(1, explorer.constantSteps(100));
    }

    @Test
    @Disabled("练习：移除 @Disabled，理解 O(n)")
    @DisplayName("线性次数等于输入规模")
    void linearStepsGrowWithSize() {
        assertEquals(0, explorer.linearSteps(0));
        assertEquals(5, explorer.linearSteps(5));
    }

    @Test
    @Disabled("练习：移除 @Disabled，理解 O(n^2)")
    @DisplayName("平方次数等于规模乘规模")
    void quadraticStepsGrowWithSquareOfSize() {
        assertEquals(0, explorer.quadraticSteps(0));
        assertEquals(16, explorer.quadraticSteps(4));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拒绝负数规模")
    @DisplayName("拒绝负数规模")
    void rejectsNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> explorer.constantSteps(-1));
        assertThrows(IllegalArgumentException.class, () -> explorer.linearSteps(-1));
        assertThrows(IllegalArgumentException.class, () -> explorer.quadraticSteps(-1));
    }
}

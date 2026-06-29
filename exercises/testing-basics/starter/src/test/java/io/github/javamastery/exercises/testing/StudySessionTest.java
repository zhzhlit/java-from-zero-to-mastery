package io.github.javamastery.exercises.testing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StudySession")
class StudySessionTest {
    @Test
    @Disabled("练习：移除 @Disabled，并补全基础断言")
    @DisplayName("保存主题和计划分钟数，并生成稳定摘要")
    void storesTopicMinutesAndSummary() {
        StudySession session = new StudySession("JUnit assertions", 30);

        assertAll(
                () -> assertEquals("JUnit assertions", session.topic()),
                () -> assertEquals(30, session.plannedMinutes()),
                () -> assertEquals("JUnit assertions (30 min)", session.summary())
        );
    }

    @Test
    @Disabled("练习：移除 @Disabled，并补全异常测试")
    @DisplayName("拒绝空主题")
    void rejectsBlankTopic() {
        assertThrows(IllegalArgumentException.class, () -> new StudySession(" ", 30));
    }

    @ParameterizedTest(name = "{0} 分钟是否是专注学习块：{1}")
    @CsvSource({
            "24, false",
            "25, true",
            "50, true"
    })
    @Disabled("练习：移除 @Disabled，并补全参数化测试")
    @DisplayName("按 25 分钟边界判断专注学习块")
    void identifiesFocusedBlocks(int plannedMinutes, boolean expected) {
        StudySession session = new StudySession("Boundary testing", plannedMinutes);

        assertEquals(expected, session.isFocusedBlock());
    }
}

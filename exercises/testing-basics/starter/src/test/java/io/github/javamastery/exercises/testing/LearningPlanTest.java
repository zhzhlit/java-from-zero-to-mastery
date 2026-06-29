package io.github.javamastery.exercises.testing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LearningPlan")
class LearningPlanTest {
    private final LearningPlan plan = new LearningPlan();

    @Test
    @Disabled("练习：移除 @Disabled，并补全集合断言")
    @DisplayName("汇总所有学习块分钟数")
    void sumsPlannedMinutes() {
        List<StudySession> sessions = List.of(
                new StudySession("Assertions", 25),
                new StudySession("Exceptions", 30)
        );

        assertEquals(55, plan.totalMinutes(sessions));
    }

    @Test
    @Disabled("练习：移除 @Disabled，并补全 Optional 边界测试")
    @DisplayName("空计划没有最长学习块")
    void returnsEmptyWhenPlanHasNoSessions() {
        Optional<StudySession> longest = plan.longestSession(List.of());

        assertTrue(longest.isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，并补全大小写无关查询测试")
    @DisplayName("按关键字查找学习主题时忽略大小写")
    void findsTopicIgnoringCase() {
        List<StudySession> sessions = List.of(new StudySession("JUnit Parameterized Tests", 40));

        assertTrue(plan.containsTopic(sessions, "junit"));
        assertFalse(plan.containsTopic(sessions, "maven"));
    }
}

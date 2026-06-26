package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyGoalTest {

    @Test
    void createsGoalWithValidatedState() {
        StudyGoal goal = new StudyGoal("  封装练习  ", 60);

        assertEquals("封装练习", goal.getTitle());
        assertEquals(60, goal.getTargetMinutes());
        assertEquals(0, goal.getCompletedMinutes());
        assertEquals(60, goal.remainingMinutes());
        assertFalse(goal.isAchieved());
    }

    @Test
    void recordsMinutesThroughControlledOperation() {
        StudyGoal goal = new StudyGoal("每日学习", 45);

        goal.recordMinutes(20);
        goal.recordMinutes(30);

        assertEquals(45, goal.getCompletedMinutes());
        assertEquals(0, goal.remainingMinutes());
        assertEquals(1.0, goal.completionRate(), 0.0001);
        assertTrue(goal.isAchieved());
        assertEquals("每日学习: 45/45 min (100%)", goal.summary());
    }

    @Test
    void changingTargetPreservesInvariant() {
        StudyGoal goal = new StudyGoal("调试复盘", 60);
        goal.recordMinutes(45);

        goal.changeTargetMinutes(30);

        assertEquals(30, goal.getTargetMinutes());
        assertEquals(30, goal.getCompletedMinutes());
        assertEquals(0, goal.remainingMinutes());
        assertTrue(goal.isAchieved());
    }

    @Test
    void renamesGoalThroughValidatedOperation() {
        StudyGoal goal = new StudyGoal("旧标题", 30);

        goal.rename("  新标题  ");

        assertEquals("新标题", goal.getTitle());
    }

    @Test
    void rejectsInvalidStateChanges() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudyGoal(" ", 30));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyGoal("封装", 0));

        StudyGoal goal = new StudyGoal("封装", 30);
        assertThrows(IllegalArgumentException.class,
                () -> goal.rename(null));
        assertThrows(IllegalArgumentException.class,
                () -> goal.changeTargetMinutes(-1));
        assertThrows(IllegalArgumentException.class,
                () -> goal.recordMinutes(0));
    }
}

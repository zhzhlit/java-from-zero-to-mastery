package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyGoalTest {

    @Test
    void createsGoalWithTrimmedTitle() {
        StudyGoal goal = new StudyGoal("  每日 Java  ", 50);

        assertEquals("每日 Java", goal.getTitle());
        assertEquals(50, goal.getTargetMinutes());
        assertEquals(0, goal.getCompletedMinutes());
        assertEquals(50, goal.remainingMinutes());
        assertFalse(goal.isAchieved());
    }

    @Test
    void recordsProgressWithoutExceedingTarget() {
        StudyGoal goal = new StudyGoal("封装练习", 40);

        goal.recordMinutes(15);
        goal.recordMinutes(30);

        assertEquals(40, goal.getCompletedMinutes());
        assertEquals(0, goal.remainingMinutes());
        assertEquals(1.0, goal.completionRate(), 0.0001);
        assertTrue(goal.isAchieved());
    }

    @Test
    void keepsCompletedMinutesInsideTargetWhenTargetChanges() {
        StudyGoal goal = new StudyGoal("复盘", 60);
        goal.recordMinutes(45);

        goal.changeTargetMinutes(30);

        assertEquals(30, goal.getTargetMinutes());
        assertEquals(30, goal.getCompletedMinutes());
        assertEquals("复盘: 30/30 min (100%)", goal.summary());
    }

    @Test
    void renamesThroughValidatedMethod() {
        StudyGoal goal = new StudyGoal("旧目标", 30);

        goal.rename("  新目标  ");

        assertEquals("新目标", goal.getTitle());
    }

    @Test
    void rejectsInvalidGoalState() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudyGoal("", 30));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyGoal("封装", 0));

        StudyGoal goal = new StudyGoal("封装", 30);
        assertThrows(IllegalArgumentException.class,
                () -> goal.rename(" "));
        assertThrows(IllegalArgumentException.class,
                () -> goal.changeTargetMinutes(-1));
        assertThrows(IllegalArgumentException.class,
                () -> goal.recordMinutes(0));
    }
}

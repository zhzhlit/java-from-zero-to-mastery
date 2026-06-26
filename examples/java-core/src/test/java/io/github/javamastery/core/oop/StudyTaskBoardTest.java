package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudyTaskBoardTest {

    @Test
    void createsTaskWithEnumOptionalAndSummary() {
        StudyTask task = new StudyTask("  Stream 复盘  ",
                StudyTaskStatus.TODO,
                StudyTaskPriority.HIGH,
                LocalDate.of(2026, 7, 5));

        assertEquals("Stream 复盘", task.title());
        assertEquals(StudyTaskStatus.TODO, task.status());
        assertEquals("待开始", task.status().displayName());
        assertTrue(task.status().open());
        assertEquals(3, task.priority().weight());
        assertEquals(Optional.of(LocalDate.of(2026, 7, 5)), task.dueDate());
        assertEquals("高 | 待开始 | Stream 复盘 | due 2026-07-05", task.summary());
        assertTrue(task.overdueOn(LocalDate.of(2026, 7, 6)));
    }

    @Test
    void supportsTaskWithoutDueDateAndStatusChange() {
        StudyTask task = new StudyTask("整理错题",
                StudyTaskStatus.IN_PROGRESS,
                StudyTaskPriority.MEDIUM,
                null);

        StudyTask completed = task.withStatus(StudyTaskStatus.DONE);

        assertEquals(Optional.empty(), task.dueDate());
        assertFalse(task.overdueOn(LocalDate.of(2026, 7, 6)));
        assertEquals("中 | 进行中 | 整理错题", task.summary());
        assertEquals(StudyTaskStatus.DONE, completed.status());
        assertFalse(completed.status().open());
        assertEquals("中 | 已完成 | 整理错题", completed.summary());
    }

    @Test
    void sortsOpenTasksByPriorityDueDateAndTitle() {
        StudyTaskBoard board = new StudyTaskBoard();
        StudyTask low = new StudyTask("整理笔记", StudyTaskStatus.TODO,
                StudyTaskPriority.LOW, LocalDate.of(2026, 7, 2));
        StudyTask highLater = new StudyTask("复盘 Stream", StudyTaskStatus.IN_PROGRESS,
                StudyTaskPriority.HIGH, LocalDate.of(2026, 7, 5));
        StudyTask highEarlier = new StudyTask("完成枚举练习", StudyTaskStatus.TODO,
                StudyTaskPriority.HIGH, LocalDate.of(2026, 7, 1));
        StudyTask done = new StudyTask("阅读文件 I/O", StudyTaskStatus.DONE,
                StudyTaskPriority.HIGH, LocalDate.of(2026, 6, 30));

        board.addTask(low);
        board.addTask(highLater);
        board.addTask(highEarlier);
        board.addTask(done);

        assertEquals(List.of(highEarlier, highLater, low), board.openTasksByPriority());
        assertThrows(UnsupportedOperationException.class,
                () -> board.tasks().add(low));
    }

    @Test
    void findsTasksAndBuildsStatusSummary() {
        StudyTaskBoard board = new StudyTaskBoard();
        StudyTask todo = new StudyTask("枚举", StudyTaskStatus.TODO,
                StudyTaskPriority.HIGH, LocalDate.of(2026, 7, 1));
        StudyTask progress = new StudyTask("Optional", StudyTaskStatus.IN_PROGRESS,
                StudyTaskPriority.MEDIUM, null);
        StudyTask done = new StudyTask("StringJoiner", StudyTaskStatus.DONE,
                StudyTaskPriority.LOW, LocalDate.of(2026, 6, 30));

        board.addTask(todo);
        board.addTask(progress);
        board.addTask(done);

        assertEquals(Optional.of(progress), board.findByTitle(" optional "));
        assertEquals(Optional.empty(), board.findByTitle("missing"));
        assertEquals("待开始=1, 进行中=1, 已完成=1, 受阻=0", board.statusSummary());
    }

    @Test
    void findsFirstOverdueOpenTask() {
        StudyTaskBoard board = new StudyTaskBoard();
        StudyTask done = new StudyTask("已完成任务", StudyTaskStatus.DONE,
                StudyTaskPriority.HIGH, LocalDate.of(2026, 7, 1));
        StudyTask blocked = new StudyTask("等待答疑", StudyTaskStatus.BLOCKED,
                StudyTaskPriority.HIGH, LocalDate.of(2026, 7, 3));
        StudyTask todo = new StudyTask("补充测试", StudyTaskStatus.TODO,
                StudyTaskPriority.MEDIUM, LocalDate.of(2026, 7, 2));

        board.addTask(done);
        board.addTask(blocked);
        board.addTask(todo);

        assertEquals(Optional.of(blocked), board.firstOverdueTask(LocalDate.of(2026, 7, 6)));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudyTask(" ", StudyTaskStatus.TODO, StudyTaskPriority.HIGH, null));
        assertThrows(NullPointerException.class,
                () -> new StudyTask("枚举", null, StudyTaskPriority.HIGH, null));
        assertThrows(NullPointerException.class,
                () -> new StudyTask("枚举", StudyTaskStatus.TODO, null, null));

        StudyTaskBoard board = new StudyTaskBoard();
        assertThrows(NullPointerException.class,
                () -> board.addTask(null));
        assertThrows(IllegalArgumentException.class,
                () -> board.findByTitle(""));
        assertThrows(NullPointerException.class,
                () -> board.firstOverdueTask(null));

        StudyTask task = new StudyTask("枚举", StudyTaskStatus.TODO, StudyTaskPriority.HIGH, null);
        assertThrows(NullPointerException.class,
                () -> task.withStatus(null));
        assertThrows(NullPointerException.class,
                () -> task.overdueOn(null));
    }
}

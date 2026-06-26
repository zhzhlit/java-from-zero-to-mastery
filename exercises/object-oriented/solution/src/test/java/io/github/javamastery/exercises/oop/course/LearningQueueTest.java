package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LearningQueueTest {

    @Test
    void storesLessonsWithConcreteElementType() {
        Lesson collections = new Lesson("集合", 40);
        Lesson generics = new Lesson("泛型", 45);
        LearningQueue<Lesson> queue = new LearningQueue<>();

        queue.add(collections);
        queue.add(generics);

        assertEquals(2, queue.size());
        assertFalse(queue.isEmpty());
        assertSame(collections, queue.peekNext());
        assertEquals(List.of(collections, generics), queue.remainingItems());
        assertSame(collections, queue.completeNext());
        assertSame(generics, queue.peekNext());
    }

    @Test
    void sameGenericClassWorksForReviewResults() {
        ReviewResult quiz = new QuizResult("泛型基础", 8, 10);
        ReviewResult review = new CodeReviewResult("泛型练习", 5, 5);
        LearningQueue<ReviewResult> queue = new LearningQueue<>();

        queue.add(quiz);
        queue.add(review);

        assertEquals("泛型基础", queue.completeNext().title());
        assertEquals("泛型练习", queue.completeNext().title());
        assertTrue(queue.isEmpty());
    }

    @Test
    void keepsReturnedItemsReadOnly() {
        LearningQueue<Lesson> queue = new LearningQueue<>();
        queue.add(new Lesson("集合", 40));

        List<Lesson> remaining = queue.remainingItems();

        assertThrows(UnsupportedOperationException.class,
                () -> remaining.add(new Lesson("泛型", 45)));
        assertEquals(1, queue.size());
    }

    @Test
    void rejectsNullItemsAndEmptyQueueAccess() {
        LearningQueue<Lesson> queue = new LearningQueue<>();

        assertThrows(IllegalArgumentException.class,
                () -> queue.add(null));
        assertThrows(IllegalStateException.class, queue::peekNext);
        assertThrows(IllegalStateException.class, queue::completeNext);
    }
}

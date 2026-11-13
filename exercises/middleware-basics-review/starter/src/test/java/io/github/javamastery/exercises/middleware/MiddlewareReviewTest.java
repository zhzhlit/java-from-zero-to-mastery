package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.messaging.CoursePublishedEvent;
import io.github.javamastery.exercises.messaging.QueuedMessage;
import io.github.javamastery.exercises.search.CourseSearchIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@org.junit.jupiter.api.Disabled("练习：完成发布事务及综合消费流程后移除 @Disabled")
class MiddlewareReviewTest {
    private static final String STREAM = "learning:review:test";
    private final JedisPooled redis = mock(JedisPooled.class);
    private ReviewFixture fixture;
    private CoursePublicationWorkflow workflow;

    @BeforeEach
    void prepare() throws IOException {
        fixture = new ReviewFixture(STREAM);
        workflow = new CoursePublicationWorkflow(fixture.source, fixture.index, redis, "review");
        when(redis.xack(eq(STREAM), eq("review"), any(StreamEntryID.class))).thenReturn(1L);
    }

    @AfterEach
    void close() throws IOException {
        fixture.close();
    }

    @Test
    void courseDetailsAndPublicationIntentCommitWithoutRedis() {
        var course = fixture.course(1);
        var event = fixture.source.release(course);
        assertEquals(course, fixture.source.findDocument(1).orElseThrow());
        assertEquals(course.title(), fixture.source.findById(1).orElseThrow().title());
        assertEquals(event, fixture.source.outbox().pendingBatch(10).getFirst().event());
        verifyNoInteractions(redis);
    }

    @Test
    void outboxFailureRollsBackCourseAndDetails() {
        fixture.jdbc.execute("ALTER TABLE publication_outbox ADD CONSTRAINT reject_release CHECK (course_id <> 1)");
        assertThrows(DataIntegrityViolationException.class, () -> fixture.source.release(fixture.course(1)));
        assertTrue(fixture.source.findDocument(1).isEmpty());
        assertEquals(0, fixture.jdbc.queryForObject("SELECT COUNT(*) FROM review_course_details", Integer.class));
        assertTrue(fixture.source.outbox().pendingBatch(10).isEmpty());
        fixture.source.release(fixture.course(2));
        assertEquals(1, fixture.source.outbox().pendingBatch(10).size());
    }

    @Test
    void wrongOrOuterTransactionsFailBeforeWrites() throws Exception {
        try (var unrelated = new ReviewFixture("other")) {
            var wrongManager = new ReviewCourseCatalog(fixture.jdbc, unrelated.transactions, STREAM);
            assertThrows(IllegalStateException.class, () -> wrongManager.release(fixture.course(1)));
        }
        assertThrows(IllegalStateException.class, () -> fixture.transactions.executeWithoutResult(
                status -> fixture.source.release(fixture.course(1))));
        assertThrows(IllegalArgumentException.class, () -> fixture.source.findDocument(0));
        assertThrows(NullPointerException.class, () -> fixture.source.release(null));
        assertTrue(fixture.source.findDocument(1).isEmpty());
    }

    @Test
    void healthyWorkflowProjectsThenStoresNotificationThenAcknowledges() throws IOException {
        var event = fixture.source.release(fixture.course(1));
        var result = workflow.processBatch(List.of(message(event)));
        assertEquals(new CoursePublicationWorkflow.BatchResult(1, 1, 0), result);
        assertEquals(fixture.course(1), fixture.index.search("java", null, 10).hits().getFirst().course());
        assertEquals(1, fixture.notifications());
        assertEquals(1, fixture.receipts());
        verify(redis).xack(STREAM, "review", new StreamEntryID("1000-1"));
    }

    @Test
    void indexCommitFailureDoesNotWriteReceiptOrAcknowledge() throws IOException {
        var event = fixture.source.release(fixture.course(1));
        CourseSearchIndex failing = spy(fixture.index);
        doThrow(new IOException("index disk unavailable")).doCallRealMethod().when(failing).commit();
        var retrying = new CoursePublicationWorkflow(fixture.source, failing, redis, "review");
        assertThrows(IOException.class, () -> retrying.processBatch(List.of(message(event))));
        assertEquals(0, fixture.notifications());
        assertEquals(0, fixture.receipts());
        verify(redis, never()).xack(eq(STREAM), eq("review"), any(StreamEntryID.class));
        assertEquals(1, retrying.processBatch(List.of(message(event))).newNotifications());
        assertEquals(1, fixture.index.search("java", null, 10).totalHits());
    }

    @Test
    void databaseFailureAfterIndexCommitRemainsReplayable() throws IOException {
        var event = fixture.source.release(fixture.course(1));
        fixture.jdbc.execute("ALTER TABLE course_notifications ADD CONSTRAINT reject_notification CHECK (course_id <> 1)");
        assertThrows(DataIntegrityViolationException.class, () -> workflow.processBatch(List.of(message(event))));
        assertEquals(1, fixture.index.search("java", null, 10).totalHits());
        assertEquals(0, fixture.receipts());
        verify(redis, never()).xack(eq(STREAM), eq("review"), any(StreamEntryID.class));
        fixture.jdbc.execute("ALTER TABLE course_notifications DROP CONSTRAINT reject_notification");
        assertEquals(1, workflow.processBatch(List.of(message(event))).newNotifications());
        assertEquals(1, fixture.index.search("java", null, 10).totalHits());
    }

    @Test
    void ackFailureReplaysIndexButDoesNotDuplicateNotification() throws IOException {
        var event = fixture.source.release(fixture.course(1));
        when(redis.xack(eq(STREAM), eq("review"), any(StreamEntryID.class)))
                .thenThrow(new JedisConnectionException("ack unavailable")).thenReturn(1L);
        assertThrows(JedisConnectionException.class, () -> workflow.processBatch(List.of(message(event))));
        assertEquals(1, fixture.receipts());
        assertEquals(1, workflow.processBatch(List.of(message(event))).duplicates());
        assertEquals(1, fixture.notifications());
        assertEquals(1, fixture.index.search("java", null, 10).totalHits());
    }

    @Test
    void badOrMismatchedEventsFailBeforeProjectionAndAck() throws IOException {
        fixture.source.release(fixture.course(1));
        var bad = new QueuedMessage(new StreamEntryID("1000-1"), Map.of("schemaVersion", "unsupported"));
        assertThrows(IllegalArgumentException.class, () -> workflow.processBatch(List.of(bad)));
        var mismatch = new CoursePublishedEvent(UUID.randomUUID(), 1, "Wrong title");
        assertThrows(IllegalStateException.class, () -> workflow.processBatch(List.of(message(mismatch))));
        var missing = new CoursePublishedEvent(UUID.randomUUID(), 99, "Missing");
        assertThrows(IllegalStateException.class, () -> workflow.processBatch(List.of(message(missing))));
        assertEquals(0, fixture.index.search("java", null, 10).totalHits());
        assertEquals(0, fixture.receipts());
        verify(redis, never()).xack(eq(STREAM), eq("review"), any(StreamEntryID.class));
    }

    @Test
    void backfillUsesAuthorityWithoutChangingReceiptsAndGuardsBatchBoundary() throws IOException {
        var event = fixture.source.release(fixture.course(1));
        fixture.source.release(fixture.course(2));
        assertEquals(2, workflow.backfill());
        assertEquals(2, fixture.index.search("java", null, 10).totalHits());
        assertEquals(0, fixture.receipts());
        fixture.transactions.executeWithoutResult(status -> assertThrows(IllegalStateException.class, workflow::backfill));
        assertThrows(IllegalArgumentException.class,
                () -> workflow.processBatch(Collections.nCopies(101, message(event))));
        verifyNoInteractions(redis);
    }

    private QueuedMessage message(CoursePublishedEvent event) {
        return new QueuedMessage(new StreamEntryID("1000-1"), event.toFields());
    }
}

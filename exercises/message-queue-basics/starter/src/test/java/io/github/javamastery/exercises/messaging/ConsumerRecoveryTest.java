package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.XAutoClaimParams;
import redis.clients.jedis.resps.StreamEntry;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Disabled("练习：完成接管、Inbox 和消费者后移除 @Disabled")
class ConsumerRecoveryTest {
    private static final String STREAM = "learning:mq:test";
    private final JedisPooled redis = mock(JedisPooled.class);
    private final CourseEventQueue queue = new CourseEventQueue(redis, STREAM, "notifications");
    private final CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), 1, "Java Inbox");
    private final StreamEntryID id = new StreamEntryID("1000-1");
    private InboxFixture fixture;

    @BeforeEach
    void createDatabase() {
        fixture = new InboxFixture();
    }

    @AfterEach
    void closeDatabase() {
        fixture.close();
    }

    @Test
    void claimPreservesCursorAndDoesNotAcknowledge() {
        StreamEntryID next = new StreamEntryID("1000-9");
        when(redis.xautoclaim(eq(STREAM), eq("notifications"), eq("replacement"), eq(30000L),
                eq(new StreamEntryID("0-0")), any(XAutoClaimParams.class)))
                .thenReturn(Map.entry(next, List.of(new StreamEntry(id, event.toFields()))));
        ClaimBatch batch = queue.claimIdle("replacement", Duration.ofSeconds(30), new StreamEntryID("0-0"), 10);
        assertEquals(next, batch.nextStart());
        assertEquals(id, batch.messages().getFirst().id());
        when(redis.xautoclaim(eq(STREAM), eq("notifications"), eq("replacement"), eq(30000L),
                eq(next), any(XAutoClaimParams.class))).thenReturn(Map.entry(next, List.of()));
        ClaimBatch empty = queue.claimIdle("replacement", Duration.ofSeconds(30), next, 10);
        assertTrue(empty.messages().isEmpty());
        assertEquals(next, empty.nextStart());
        verify(redis, never()).xack(eq(STREAM), eq("notifications"), any(StreamEntryID.class));
    }

    @Test
    void rejectsInvalidClaimBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> queue.claimIdle("replacement", Duration.ofSeconds(-1), id, 10));
        assertThrows(IllegalArgumentException.class,
                () -> queue.claimIdle("replacement", Duration.ofNanos(1), id, 10));
        assertThrows(IllegalArgumentException.class,
                () -> queue.claimIdle("replacement", Duration.ZERO, id, 101));
        verifyNoInteractions(redis);
    }

    @Test
    void differentDeliveryIdsForSameEventCreateOnlyOneEffect() {
        StreamEntryID secondId = new StreamEntryID("1000-2");
        when(redis.xack(STREAM, "notifications", id)).thenReturn(1L);
        when(redis.xack(STREAM, "notifications", secondId)).thenReturn(1L);
        var result = new NotificationConsumer(queue, fixture.inbox).processBatch(List.of(
                new QueuedMessage(id, event.toFields()), new QueuedMessage(secondId, event.toFields())));
        assertEquals(new NotificationConsumer.ProcessResult(1, 1, 2), result);
        assertEquals(1, fixture.notifications());
        assertEquals(1, fixture.receipts());
    }

    @Test
    void failedEffectRollsBackReceiptAndCanBeRetried() {
        fixture.jdbc.execute("ALTER TABLE course_notifications ADD CONSTRAINT reject_course_one CHECK (course_id <> 1)");
        assertThrows(DataIntegrityViolationException.class, () -> fixture.inbox.storeIfNew(event));
        assertEquals(0, fixture.receipts());
        assertEquals(0, fixture.notifications());
        fixture.jdbc.execute("ALTER TABLE course_notifications DROP CONSTRAINT reject_course_one");
        assertTrue(fixture.inbox.storeIfNew(event));
        assertEquals(1, fixture.notifications());
    }

    @Test
    void acknowledgementFailureReplaysWithoutRepeatingEffect() {
        when(redis.xack(STREAM, "notifications", id))
                .thenThrow(new JedisConnectionException("ack unavailable")).thenReturn(0L, 1L);
        NotificationConsumer consumer = new NotificationConsumer(queue, fixture.inbox);
        List<QueuedMessage> batch = List.of(new QueuedMessage(id, event.toFields()));
        assertThrows(JedisConnectionException.class, () -> consumer.processBatch(batch));
        assertEquals(1, fixture.notifications());
        assertThrows(IllegalStateException.class, () -> consumer.processBatch(batch));
        assertEquals(new NotificationConsumer.ProcessResult(0, 1, 1), consumer.processBatch(batch));
        assertEquals(1, fixture.notifications());
    }

    @Test
    void malformedMessageIsNotStoredOrAcknowledged() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationConsumer(queue, fixture.inbox)
                .processBatch(List.of(new QueuedMessage(id, Map.of()))));
        assertEquals(0, fixture.receipts());
        verifyNoInteractions(redis);
    }

    @Test
    void inboxScopesAreIndependent() {
        assertTrue(fixture.inbox.storeIfNew(event));
        assertTrue(new NotificationInbox(fixture.jdbc, fixture.transactions, "other-handler").storeIfNew(event));
        assertEquals(2, fixture.notifications());
    }

    @Test
    void outerTransactionIsRejectedBeforeEffectsOrAcknowledgement() {
        fixture.transactions.executeWithoutResult(status -> assertThrows(IllegalStateException.class,
                () -> new NotificationConsumer(queue, fixture.inbox)
                        .processBatch(List.of(new QueuedMessage(id, event.toFields())))));
        assertEquals(0, fixture.receipts());
        verifyNoInteractions(redis);
    }

    @Test
    void reusedEventIdWithDifferentPayloadFailsExplicitly() {
        fixture.inbox.storeIfNew(event);
        CoursePublishedEvent conflicting = new CoursePublishedEvent(event.eventId(), 2, "Different");
        assertThrows(IllegalStateException.class, () -> fixture.inbox.storeIfNew(conflicting));
        assertEquals(1, fixture.notifications());
    }

    @Test
    void concurrentCopiesCreateOneEffect() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> { start.await(); return fixture.inbox.storeIfNew(event); });
            var second = executor.submit(() -> { start.await(); return fixture.inbox.storeIfNew(event); });
            start.countDown();
            int applied = (first.get(5, TimeUnit.SECONDS) ? 1 : 0) + (second.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, applied);
            assertEquals(1, fixture.notifications());
            assertEquals(1, fixture.receipts());
        }
    }

    @Test
    void fileDatabaseKeepsDeduplicationAcrossReopen(@TempDir Path directory) {
        String url = "jdbc:h2:file:" + directory.resolve("inbox") + ";DB_CLOSE_DELAY=-1";
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("inbox-schema.sql")).execute(dataSource);
        try {
            new NotificationInbox(jdbc, new TransactionTemplate(new DataSourceTransactionManager(dataSource)),
                    "notifications").storeIfNew(event);
            jdbc.execute("SHUTDOWN");
            DriverManagerDataSource reopened = new DriverManagerDataSource(url, "sa", "");
            JdbcTemplate newJdbc = new JdbcTemplate(reopened);
            assertFalse(new NotificationInbox(newJdbc,
                    new TransactionTemplate(new DataSourceTransactionManager(reopened)), "notifications").storeIfNew(event));
            assertEquals(1, newJdbc.queryForObject("SELECT COUNT(*) FROM course_notifications", Integer.class));
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }

    @Test
    void mismatchedGroupAndOversizedBatchAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationConsumer(
                new CourseEventQueue(redis, STREAM, "other-group"), fixture.inbox));
        assertThrows(IllegalArgumentException.class, () -> new NotificationConsumer(queue, fixture.inbox)
                .processBatch(java.util.Collections.nCopies(101, new QueuedMessage(id, event.toFields()))));
        verifyNoInteractions(redis);
    }
}

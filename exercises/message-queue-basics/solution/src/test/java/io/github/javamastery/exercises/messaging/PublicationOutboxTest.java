package io.github.javamastery.exercises.messaging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicationOutboxTest {
    private static final String STREAM = "learning:mq:publication-test";
    private PublicationFixture fixture;

    @BeforeEach
    void createDatabase() {
        fixture = new PublicationFixture(STREAM);
    }

    @AfterEach
    void closeDatabase() {
        fixture.close();
    }

    @Test
    void businessAndImmutableEventCommitTogetherWithoutRedis() {
        CoursePublishedEvent event = fixture.service.publish(1, " First Title ");
        assertEquals(1, fixture.count("courses"));
        assertEquals(1, fixture.count("outbox"));
        fixture.jdbc.update("UPDATE published_courses SET title = 'Later Title' WHERE id = 1");
        assertEquals(event, fixture.outbox.pendingBatch(10).getFirst().event());
        assertEquals("First Title", event.title());
        assertEquals(0, fixture.outbox.pendingBatch(10).getFirst().attempts());
    }

    @Test
    void failedOutboxInsertRollsBackBusinessInsert() {
        PublicationOutbox failing = spy(fixture.outbox);
        doThrow(new DataAccessResourceFailureException("outbox unavailable")).when(failing).enqueue(any());
        var service = new CoursePublishingService(fixture.jdbc, failing, fixture.transactions);
        assertThrows(DataAccessResourceFailureException.class, () -> service.publish(1, "Rollback"));
        assertEquals(0, fixture.count("courses"));
        assertEquals(0, fixture.count("outbox"));
    }

    @Test
    void failedBusinessInsertDoesNotEnqueueAnotherEvent() {
        fixture.service.publish(1, "First");
        assertThrows(DuplicateKeyException.class, () -> fixture.service.publish(1, "Duplicate"));
        assertEquals(1, fixture.count("courses"));
        assertEquals(1, fixture.count("outbox"));
    }

    @Test
    void transactionGuardsRejectUnboundResourceBeforeEitherWrite() {
        CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), 1, "Guard");
        assertThrows(IllegalStateException.class, () -> fixture.outbox.enqueue(event));
        assertThrows(IllegalStateException.class, () -> fixture.transactions.executeWithoutResult(
                status -> fixture.service.publish(1, "Nested")));
        try (PublicationFixture unrelated = new PublicationFixture("other")) {
            var misconfigured = new CoursePublishingService(fixture.jdbc, fixture.outbox, unrelated.transactions);
            assertThrows(IllegalStateException.class, () -> misconfigured.publish(1, "Wrong manager"));
            assertThrows(IllegalArgumentException.class,
                    () -> new CoursePublishingService(fixture.jdbc, unrelated.outbox, fixture.transactions));
        }
        TransactionTemplate supports = new TransactionTemplate(fixture.transactions.getTransactionManager());
        supports.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        assertThrows(IllegalStateException.class,
                () -> new CoursePublishingService(fixture.jdbc, fixture.outbox, supports).publish(1, "No actual transaction"));
        assertEquals(0, fixture.count("courses"));
        assertEquals(0, fixture.count("outbox"));
    }

    @Test
    void invalidRequestsAndConfigurationDoNotWrite() {
        assertThrows(IllegalArgumentException.class, () -> fixture.service.publish(0, "Invalid"));
        assertThrows(IllegalArgumentException.class, () -> fixture.service.publish(1, " "));
        assertThrows(IllegalArgumentException.class, () -> new PublicationOutbox(fixture.jdbc, "bad stream"));
        assertThrows(IllegalArgumentException.class, () -> fixture.outbox.pendingBatch(0));
        assertThrows(IllegalArgumentException.class, () -> fixture.outbox.pendingBatch(101));
        assertEquals(0, fixture.count("courses"));
    }

    @Test
    void pendingSelectionSeparatesDestinationsAndPrioritizesUntriedTasks() {
        CoursePublishedEvent first = fixture.service.publish(1, "First");
        CoursePublishedEvent second = fixture.service.publish(2, "Second");
        PublicationOutbox other = new PublicationOutbox(fixture.jdbc, "other-stream");
        CoursePublishedEvent third = new CoursePublishingService(fixture.jdbc, other, fixture.transactions).publish(3, "Other");
        fixture.outbox.markFailed(first.eventId());
        assertEquals(second, fixture.outbox.pendingBatch(1).getFirst().event());
        assertEquals(2, fixture.outbox.pendingBatch(10).size());
        assertEquals(third, other.pendingBatch(10).getFirst().event());
        assertEquals(1, fixture.outbox.pendingBatch(10).getLast().attempts());
        assertEquals("REDIS_PUBLISH_FAILED", fixture.jdbc.queryForObject(
                "SELECT error_code FROM publication_outbox WHERE event_id = ?", String.class, first.eventId().toString()));
    }

    @Test
    void publicationMarkerStoresBrokerIdentityAndRejectsMissingOrFinishedTasks() {
        CoursePublishedEvent event = fixture.service.publish(1, "Marker");
        fixture.outbox.markFailed(event.eventId());
        fixture.outbox.markPublished(event.eventId(), new StreamEntryID("1000-1"));
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertEquals("1000-1", fixture.jdbc.queryForObject("SELECT stream_id FROM publication_outbox", String.class));
        assertEquals(2, fixture.jdbc.queryForObject("SELECT attempts FROM publication_outbox", Integer.class));
        assertNull(fixture.jdbc.queryForObject("SELECT error_code FROM publication_outbox", String.class));
        assertThrows(IllegalStateException.class, () -> fixture.outbox.markFailed(event.eventId()));
        assertThrows(IllegalStateException.class, () -> fixture.outbox.markPublished(UUID.randomUUID(), new StreamEntryID("1000-2")));
    }

    @Test
    void brokerFailureIsRecordedAndDoesNotBlockNextTask() {
        CoursePublishedEvent first = fixture.service.publish(1, "Retry");
        CoursePublishedEvent second = fixture.service.publish(2, "Healthy");
        CourseEventQueue queue = queue();
        JedisConnectionException failure = new JedisConnectionException("connection unavailable");
        when(queue.publish(first)).thenThrow(failure);
        when(queue.publish(second)).thenReturn(new StreamEntryID("1000-2"));
        var result = new PublicationRelay(fixture.outbox, queue).relayBatch(10);
        assertEquals(1, result.published());
        assertSame(failure, result.failures().getFirst().cause());
        assertEquals(first.eventId(), result.failures().getFirst().eventId());
        assertEquals(new PublicationTask(first, 1), fixture.outbox.pendingBatch(10).getFirst());
    }

    @Test
    void failedDatabaseAcknowledgementKeepsSameEventReplayable() {
        CoursePublishedEvent event = fixture.service.publish(1, "Replay");
        CourseEventQueue queue = queue();
        when(queue.publish(event)).thenReturn(new StreamEntryID("1000-1"));
        PublicationOutbox failing = spy(fixture.outbox);
        doThrow(new DataAccessResourceFailureException("ack failed")).when(failing).markPublished(any(), any());
        assertThrows(DataAccessResourceFailureException.class, () -> new PublicationRelay(failing, queue).relayBatch(10));
        verify(failing, org.mockito.Mockito.never()).markFailed(any());
        assertEquals(new PublicationTask(event, 0), fixture.outbox.pendingBatch(10).getFirst());
        assertEquals(1, new PublicationRelay(fixture.outbox, queue).relayBatch(10).published());
    }

    @Test
    void failureBookkeepingErrorRetainsOriginalBrokerError() {
        fixture.service.publish(1, "Retry");
        CourseEventQueue queue = queue();
        JedisConnectionException brokerFailure = new JedisConnectionException("broker down");
        when(queue.publish(any())).thenThrow(brokerFailure);
        PublicationOutbox failing = spy(fixture.outbox);
        doThrow(new DataAccessResourceFailureException("database down")).when(failing).markFailed(any());
        var thrown = assertThrows(DataAccessResourceFailureException.class,
                () -> new PublicationRelay(failing, queue).relayBatch(10));
        assertSame(brokerFailure, thrown.getSuppressed()[0]);
        assertEquals(1, fixture.outbox.pendingBatch(10).size());
    }

    @Test
    void relayRejectsOuterTransactionsAndUnknownFailuresAreNotClassifiedAsRedis() {
        CourseEventQueue queue = queue();
        PublicationRelay relay = new PublicationRelay(fixture.outbox, queue);
        assertThrows(IllegalStateException.class,
                () -> fixture.transactions.executeWithoutResult(status -> relay.relayBatch(10)));
        assertThrows(IllegalArgumentException.class, () -> relay.relayBatch(101));
        when(queue.streamName()).thenReturn("other");
        assertThrows(IllegalArgumentException.class, () -> new PublicationRelay(fixture.outbox, queue));
        when(queue.streamName()).thenReturn(STREAM);
        fixture.service.publish(1, "Unknown");
        when(queue.publish(any())).thenThrow(new IllegalStateException("configuration problem"));
        assertThrows(IllegalStateException.class, () -> relay.relayBatch(10));
        assertEquals(0, fixture.outbox.pendingBatch(10).getFirst().attempts());
    }

    @Test
    void committedOutboxSurvivesFileDatabaseCloseAndReopen(@TempDir Path directory) {
        String url = "jdbc:h2:file:" + directory.resolve("publication").toAbsolutePath();
        CoursePublishedEvent event;
        try (PublicationFixture file = new PublicationFixture(STREAM, url)) {
            event = file.service.publish(1, "Persistent");
        }
        JdbcTemplate reopened = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
        try {
            PublicationOutbox restored = new PublicationOutbox(reopened, STREAM);
            assertEquals(new PublicationTask(event, 0), restored.pendingBatch(10).getFirst());
            assertEquals("Persistent", reopened.queryForObject("SELECT title FROM published_courses", String.class));
        } finally {
            reopened.execute("SHUTDOWN");
        }
    }

    private CourseEventQueue queue() {
        CourseEventQueue queue = mock(CourseEventQueue.class);
        when(queue.streamName()).thenReturn(STREAM);
        return queue;
    }
}

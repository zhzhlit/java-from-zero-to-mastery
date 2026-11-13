package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.messaging.PublicationRelay;
import io.github.javamastery.exercises.search.CourseDocument;
import io.github.javamastery.exercises.search.CourseLevel;
import io.github.javamastery.exercises.search.CourseSearchIndex;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class MiddlewareReviewIT {
    private final String stream = "learning:review:it:" + UUID.randomUUID();
    private ReviewFixture fixture;
    private JedisPooled redis;
    private JedisPooled observer;
    private CoursePublicationWorkflow workflow;
    private PublicationRelay relay;

    @BeforeEach
    void prepare() throws IOException {
        fixture = new ReviewFixture(stream);
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        redis = new JedisPooled(host, port);
        observer = new JedisPooled(host, port);
        workflow = new CoursePublicationWorkflow(fixture.source, fixture.index, redis, "review");
        workflow.queue().ensureGroup();
        relay = new PublicationRelay(fixture.source.outbox(), workflow.queue());
    }

    @AfterEach
    void cleanup() throws IOException {
        try {
            if (observer != null) {
                observer.del(stream);
            }
        } finally {
            try {
                if (redis != null) redis.close();
                if (observer != null) observer.close();
            } finally {
                if (fixture != null) fixture.close();
            }
        }
    }

    @Test
    void authoritativeRpcAndEventuallyVisibleSearchFormRealPipeline() throws Exception {
        var event = fixture.source.release(fixture.course(1));
        try (var rpc = new ReviewRpcEndpoint(fixture.source)) {
            assertEquals(event.title(), rpc.client().getCourse(1).title());
            assertEquals(0, fixture.index.search("java", null, 10).totalHits());
            assertEquals(1, relay.relayBatch(10).published());
            assertEquals(1, workflow.processBatch(workflow.queue().readNew("worker", 10)).newNotifications());
            assertEquals(fixture.course(1), fixture.index.search("java", CourseLevel.INTERMEDIATE, 10).hits().getFirst().course());
            workflow.queue().publish(event);
            assertEquals(1, workflow.processBatch(workflow.queue().readNew("worker", 10)).duplicates());
            assertEquals(1, fixture.notifications());
            assertEquals(1, fixture.index.search("java", null, 10).totalHits());
            assertEquals(0, workflow.queue().pendingCount());
            assertEquals(0, fixture.source.outbox().pendingBatch(10).size());
        }
    }

    @Test
    void brokerFailureKeepsOutboxAndCanRecoverWithoutReleasingCourseAgain() throws Exception {
        fixture.source.release(fixture.course(1));
        redis.del(stream);
        redis.set(stream, "wrong type");
        assertEquals(1, relay.relayBatch(10).failures().size());
        assertEquals(1, fixture.source.outbox().pendingBatch(10).size());
        assertEquals(0, fixture.index.search("java", null, 10).totalHits());
        try (var rpc = new ReviewRpcEndpoint(fixture.source)) {
            assertEquals(fixture.course(1).title(), rpc.client().getCourse(1).title());
        }
        redis.del(stream);
        workflow.queue().ensureGroup();
        assertEquals(1, relay.relayBatch(10).published());
        assertEquals(1, workflow.processBatch(workflow.queue().readNew("worker", 10)).newNotifications());
    }

    @Test
    void ackConnectionFailureAfterBothStoresCommitReplaysIdempotently() throws IOException {
        fixture.source.release(fixture.course(1));
        relay.relayBatch(10);
        var messages = workflow.queue().readNew("worker", 10);
        CourseSearchIndex closing = spy(fixture.index);
        doAnswer(invocation -> {
            invocation.callRealMethod();
            redis.close();
            return null;
        }).when(closing).refresh();
        var interrupted = new CoursePublicationWorkflow(fixture.source, closing, redis, "review");
        assertThrows(JedisException.class, () -> interrupted.processBatch(messages));
        assertEquals(1, fixture.receipts());
        var recovered = new CoursePublicationWorkflow(fixture.source, fixture.index, observer, "review");
        assertEquals(1, recovered.queue().pendingCount());
        assertEquals(1, recovered.processBatch(recovered.queue().readOwnPending("worker", 10)).duplicates());
        assertEquals(1, fixture.notifications());
        assertEquals(1, fixture.index.search("java", null, 10).totalHits());
        assertEquals(0, recovered.queue().pendingCount());
    }

    @Test
    void failedProjectionKeepsPendingForAnotherConsumerToClaim() throws IOException {
        fixture.source.release(fixture.course(1));
        relay.relayBatch(10);
        var messages = workflow.queue().readNew("original", 10);
        CourseSearchIndex failing = spy(fixture.index);
        doThrow(new IOException("index unavailable")).when(failing).upsert(any(CourseDocument.class));
        var interrupted = new CoursePublicationWorkflow(fixture.source, failing, redis, "review");
        assertThrows(IOException.class, () -> interrupted.processBatch(messages));
        assertEquals(1, workflow.queue().pendingCount());
        assertEquals(0, fixture.receipts());
        var claimed = workflow.queue().claimIdle("replacement", Duration.ZERO, new StreamEntryID("0-0"), 10);
        assertEquals(1, workflow.processBatch(claimed.messages()).newNotifications());
        assertEquals(0, workflow.queue().pendingCount());
    }

    @Test
    void committedFileIndexReopensAndFreshIndexBackfillDoesNotDependOnReceipts(@TempDir Path path) throws IOException {
        fixture.source.release(fixture.course(1));
        fixture.source.release(fixture.course(2));
        relay.relayBatch(10);
        try (var directory = FSDirectory.open(path); var index = new CourseSearchIndex(directory)) {
            var persistent = new CoursePublicationWorkflow(fixture.source, index, redis, "review");
            assertEquals(1, persistent.processBatch(persistent.queue().readNew("worker", 1)).newNotifications());
        }
        try (var directory = FSDirectory.open(path); var index = new CourseSearchIndex(directory)) {
            assertEquals(1, index.search("java", null, 10).totalHits());
        }
        try (var directory = new ByteBuffersDirectory(); var fresh = new CourseSearchIndex(directory)) {
            var recovered = new CoursePublicationWorkflow(fixture.source, fresh, redis, "review");
            assertEquals(2, recovered.backfill());
            assertEquals(2, fresh.search("java", null, 10).totalHits());
            assertEquals(1, fixture.receipts());
            assertEquals(1, recovered.processBatch(recovered.queue().readNew("worker", 10)).newNotifications());
            assertEquals(2, fresh.search("java", null, 10).totalHits());
            assertEquals(2, fixture.notifications());
        }
    }
}

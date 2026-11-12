package io.github.javamastery.exercises.rediscache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@Disabled("练习：完成 Outbox 服务、任务存储和处理器后移除 @Disabled")
class OutboxRecoveryTest {
    private final JedisPooled redis = mock(JedisPooled.class);
    private OutboxFixture fixture;

    @BeforeEach
    void createDatabase() {
        fixture = new OutboxFixture(redis);
    }

    @AfterEach
    void closeDatabase() {
        fixture.close();
    }

    @Test
    void commitsCourseAndTaskTogetherWithoutCallingRedis() {
        fixture.service.rename(1, "Committed");
        assertEquals("Committed", fixture.db.title());
        assertEquals(fixture.db.key(), fixture.outbox.pendingBatch(10).getFirst().cacheKey());
        verifyNoInteractions(redis);
    }

    @Test
    void outerRollbackDiscardsCourseAndTask() {
        fixture.db.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Discarded");
            assertEquals(1, fixture.outbox.pendingBatch(10).size());
            status.setRollbackOnly();
        });
        assertEquals("Java Basics", fixture.db.title());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        verifyNoInteractions(redis);
    }

    @Test
    void constraintFailureDoesNotEnqueueTask() {
        assertThrows(DataIntegrityViolationException.class, () -> fixture.service.rename(1, "Spring Boot"));
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertEquals("Java Basics", fixture.db.title());
    }

    @Test
    void taskWriteFailureRollsBackCourseUpdate() {
        fixture.db.jdbc.execute("ALTER TABLE cache_invalidations ADD CONSTRAINT reject_course_one CHECK (course_id <> 1)");
        assertThrows(DataIntegrityViolationException.class, () -> fixture.service.rename(1, "Must Roll Back"));
        assertEquals("Java Basics", fixture.db.title());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
    }

    @Test
    void successfulDispatchAcknowledgesTask() {
        fixture.service.rename(1, "New");
        var result = fixture.dispatcher.dispatchBatch(10);
        assertEquals(1, result.delivered());
        assertTrue(result.failures().isEmpty());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertEquals(1, fixture.db.jdbc.queryForObject("SELECT attempts FROM cache_invalidations", Integer.class));
        verify(redis).del(fixture.db.key());
    }

    @Test
    void redisFailureRemainsPendingAndIsVisibleToCaller() {
        fixture.service.rename(1, "New");
        JedisConnectionException failure = new JedisConnectionException("redis unavailable");
        doThrow(failure).when(redis).del(fixture.db.key());
        var result = fixture.dispatcher.dispatchBatch(10);
        assertEquals(0, result.delivered());
        assertSame(failure, result.failures().getFirst().cause());
        InvalidationTask pending = fixture.outbox.pendingBatch(10).getFirst();
        assertEquals(1, pending.attempts());
        assertEquals("REDIS_DELETE_FAILED", pending.errorCode());
        assertEquals("New", fixture.db.title());
    }

    @Test
    void laterRetryRecoversFailedTask() {
        fixture.service.rename(1, "New");
        doThrow(new JedisConnectionException("temporary failure")).doReturn(1L).when(redis).del(fixture.db.key());
        assertEquals(1, fixture.dispatcher.dispatchBatch(10).failures().size());
        assertEquals(1, fixture.dispatcher.dispatchBatch(10).delivered());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertEquals(2, fixture.db.jdbc.queryForObject("SELECT attempts FROM cache_invalidations", Integer.class));
    }

    @Test
    void acknowledgementFailureLeavesTaskForIdempotentReplay() {
        fixture.service.rename(1, "New");
        InvalidationOutbox failingAck = spy(fixture.outbox);
        doThrow(new DataAccessResourceFailureException("ack failed")).doCallRealMethod()
                .when(failingAck).markDelivered(anyLong());
        OutboxDispatcher dispatcher = new OutboxDispatcher(redis, failingAck);
        assertThrows(DataAccessResourceFailureException.class, () -> dispatcher.dispatchBatch(10));
        assertEquals(1, fixture.outbox.pendingBatch(10).size());
        assertEquals(1, dispatcher.dispatchBatch(10).delivered());
        verify(redis, times(2)).del(fixture.db.key());
    }

    @Test
    void boundedBatchContinuesAfterOneRedisFailure() {
        fixture.service.rename(1, "First");
        fixture.service.rename(2, "Second");
        fixture.service.rename(1, "Third");
        doThrow(new JedisConnectionException("first key failed")).when(redis).del(fixture.db.key());
        var result = fixture.dispatcher.dispatchBatch(2);
        assertEquals(1, result.failures().size());
        assertEquals(1, result.delivered());
        var pending = fixture.outbox.pendingBatch(10);
        assertEquals(2, pending.size());
        assertEquals(1, pending.stream().mapToInt(InvalidationTask::attempts).sum());
    }

    @Test
    void rejectsInvalidBatchAndIncorrectTransactionBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> fixture.dispatcher.dispatchBatch(0));
        assertThrows(IllegalArgumentException.class, () -> fixture.dispatcher.dispatchBatch(1001));
        assertThrows(IllegalStateException.class, () -> fixture.outbox.enqueue(1, fixture.db.key()));
        fixture.db.transactions.executeWithoutResult(status ->
                assertThrows(IllegalStateException.class, () -> fixture.dispatcher.dispatchBatch(10)));
        verifyNoInteractions(redis);
    }

    @Test
    void fileDatabaseRetainsTaskAcrossCloseAndReopen(@TempDir Path directory) {
        String url = "jdbc:h2:file:" + directory.resolve("outbox") + ";DB_CLOSE_DELAY=-1";
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("transaction-schema.sql"),
                new ClassPathResource("outbox-schema.sql")).execute(dataSource);
        try {
            JdbcCourseRepository source = new JdbcCourseRepository(jdbc);
            RedisCourseCatalog catalog = new RedisCourseCatalog(source, redis, fixture.db.namespace, Duration.ofMinutes(1));
            new OutboxCourseService(source, new InvalidationOutbox(jdbc), catalog,
                    new TransactionTemplate(new DataSourceTransactionManager(dataSource))).rename(1, "Persisted");
            jdbc.execute("SHUTDOWN");
            JdbcTemplate reopened = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
            InvalidationOutbox restored = new InvalidationOutbox(reopened);
            assertEquals("Persisted", new JdbcCourseRepository(reopened).findById(1).orElseThrow().title());
            assertEquals(fixture.db.key(), restored.pendingBatch(10).getFirst().cacheKey());
            assertEquals(1, new OutboxDispatcher(redis, restored).dispatchBatch(10).delivered());
            assertTrue(restored.pendingBatch(10).isEmpty());
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

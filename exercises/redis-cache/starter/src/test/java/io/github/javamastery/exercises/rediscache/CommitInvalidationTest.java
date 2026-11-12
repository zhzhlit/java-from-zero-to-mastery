package io.github.javamastery.exercises.rediscache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@Disabled("练习：完成事务服务和提交后失效后移除 @Disabled")
class CommitInvalidationTest {
    private final JedisPooled redis = mock(JedisPooled.class);
    private TransactionFixture fixture;

    @BeforeEach
    void createDatabase() {
        fixture = new TransactionFixture(redis);
    }

    @AfterEach
    void closeDatabase() {
        fixture.close();
    }

    @Test
    void deletesAfterStandaloneCommit() {
        fixture.service.rename(1, "Committed");
        assertEquals("Committed", fixture.title());
        verify(redis).del(fixture.key());
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }

    @Test
    void waitsForOuterTransactionToCommit() {
        fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Outer Commit");
            assertEquals("Outer Commit", fixture.title());
            verifyNoInteractions(redis);
        });
        verify(redis).del(fixture.key());
    }

    @Test
    void outerExceptionRollsBackWithoutDeleting() {
        assertThrows(IllegalStateException.class, () -> fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Must Roll Back");
            throw new IllegalStateException("outer failure");
        }));
        assertEquals("Java Basics", fixture.title());
        verifyNoInteractions(redis);
    }

    @Test
    void rollbackOnlyDoesNotDelete() {
        fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Must Roll Back");
            status.setRollbackOnly();
        });
        assertEquals("Java Basics", fixture.title());
        verifyNoInteractions(redis);
    }

    @Test
    void sourceConstraintFailureDoesNotDelete() {
        assertThrows(DataIntegrityViolationException.class, () -> fixture.service.rename(1, "Spring Boot"));
        assertEquals("Java Basics", fixture.title());
        verifyNoInteractions(redis);
    }

    @Test
    void requiresActualTransactionNotJustSynchronization() {
        assertThrows(IllegalStateException.class, () -> fixture.invalidator.invalidateAfterCommit(1));
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThrows(IllegalStateException.class, () -> fixture.invalidator.invalidateAfterCommit(1));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertThrows(IllegalArgumentException.class, () -> fixture.invalidator.invalidateAfterCommit(0));
        verifyNoInteractions(redis);
    }

    @Test
    void redisFailureReachesCallerButCannotUndoCommit() {
        doThrow(new JedisConnectionException("redis unavailable")).when(redis).del(fixture.key());
        assertThrows(JedisConnectionException.class, () -> fixture.service.rename(1, "Already Committed"));
        assertEquals("Already Committed", fixture.title());
    }

    @Test
    void transactionReadsDoNotAccessOrPopulateRedis() {
        fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Uncommitted");
            assertEquals("Uncommitted", fixture.catalog.findById(1).orElseThrow().title());
            verifyNoInteractions(redis);
            status.setRollbackOnly();
        });
        assertEquals("Java Basics", fixture.title());
        verifyNoInteractions(redis);
    }

    @Test
    void rollbackDoesNotLeakCallbacksIntoNextTransaction() {
        fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Discarded");
            status.setRollbackOnly();
        });
        fixture.service.rename(1, "Next Commit");
        verify(redis, times(1)).del(fixture.key());
        assertEquals("Next Commit", fixture.title());
    }
}

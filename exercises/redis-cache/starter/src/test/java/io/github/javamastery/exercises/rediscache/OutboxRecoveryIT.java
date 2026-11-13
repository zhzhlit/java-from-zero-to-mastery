package io.github.javamastery.exercises.rediscache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import redis.clients.jedis.JedisPooled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("练习：完成 Outbox 并启动专用 Redis 后移除 @Disabled")
class OutboxRecoveryIT {
    private JedisPooled redis;
    private JedisPooled recoveryClient;
    private OutboxFixture fixture;

    @BeforeEach
    void connectAndPrimeCache() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        redis = new JedisPooled(host, port);
        recoveryClient = new JedisPooled(host, port);
        fixture = new OutboxFixture(redis);
        fixture.db.catalog.findById(1);
    }

    @AfterEach
    void cleanOwnResources() {
        try {
            if (recoveryClient != null && fixture != null) {
                recoveryClient.del(fixture.db.key());
            }
        } finally {
            try {
                if (fixture != null) {
                    fixture.close();
                }
            } finally {
                if (recoveryClient != null) {
                    recoveryClient.close();
                }
                if (redis != null) {
                    redis.close();
                }
            }
        }
    }

    @Test
    void pendingTaskEventuallyRemovesStaleSharedValue() {
        fixture.service.rename(1, "New Title");
        assertEquals("New Title", fixture.db.title());
        assertEquals("Java Basics", recoveryClient.get(fixture.db.key()));
        assertEquals(1, fixture.dispatcher.dispatchBatch(10).delivered());
        assertFalse(recoveryClient.exists(fixture.db.key()));
        assertEquals("New Title", fixture.db.catalog.findById(1).orElseThrow().title());
    }

    @Test
    void freshWorkerRecoversFailureUsingPersistedCacheKey() {
        fixture.service.rename(1, "Recovered");
        redis.close();
        var failed = fixture.dispatcher.dispatchBatch(10);
        assertEquals(1, failed.failures().size());
        assertEquals(1, fixture.outbox.pendingBatch(10).getFirst().attempts());
        assertEquals("Java Basics", recoveryClient.get(fixture.db.key()));
        OutboxDispatcher recovered = new OutboxDispatcher(recoveryClient, new InvalidationOutbox(fixture.db.jdbc));
        assertEquals(1, recovered.dispatchBatch(10).delivered());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertFalse(recoveryClient.exists(fixture.db.key()));
        assertEquals("Recovered", fixture.db.title());
    }

    @Test
    void rollbackLeavesNoTaskAndKeepsOldCache() {
        fixture.db.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Discarded");
            status.setRollbackOnly();
        });
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertEquals(0, fixture.dispatcher.dispatchBatch(10).delivered());
        assertEquals("Java Basics", fixture.db.title());
        assertEquals("Java Basics", recoveryClient.get(fixture.db.key()));
    }

    @Test
    void duplicateKeyInvalidationsConvergeToLatestTitle() {
        fixture.service.rename(1, "First Update");
        fixture.service.rename(1, "Latest Update");
        assertEquals(2, fixture.dispatcher.dispatchBatch(10).delivered());
        assertTrue(fixture.outbox.pendingBatch(10).isEmpty());
        assertEquals("Latest Update", fixture.db.catalog.findById(1).orElseThrow().title());
    }
}

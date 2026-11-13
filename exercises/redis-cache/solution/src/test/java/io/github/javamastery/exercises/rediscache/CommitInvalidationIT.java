package io.github.javamastery.exercises.rediscache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import redis.clients.jedis.JedisPooled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommitInvalidationIT {
    private JedisPooled redis;
    private JedisPooled observer;
    private TransactionFixture fixture;

    @BeforeEach
    void connectAndPrimeCache() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        redis = new JedisPooled(host, port);
        observer = new JedisPooled(host, port);
        fixture = new TransactionFixture(redis);
        assertEquals("Java Basics", fixture.catalog.findById(1).orElseThrow().title());
    }

    @AfterEach
    void cleanOwnResources() {
        try {
            if (observer != null && fixture != null) {
                observer.del(fixture.key());
            }
        } finally {
            try {
                if (fixture != null) {
                    fixture.close();
                }
            } finally {
                if (observer != null) {
                    observer.close();
                }
                if (redis != null) {
                    redis.close();
                }
            }
        }
    }

    @Test
    void sharedKeyRemainsUntilOuterCommitThenReloads() {
        fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Committed Title");
            assertEquals("Committed Title", fixture.catalog.findById(1).orElseThrow().title());
            assertEquals("Java Basics", observer.get(fixture.key()));
        });
        assertFalse(observer.exists(fixture.key()));
        assertEquals("Committed Title", fixture.catalog.findById(1).orElseThrow().title());
    }

    @Test
    void rollbackPreservesDatabaseAndCachedTitle() {
        fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Discarded");
            status.setRollbackOnly();
        });
        assertEquals("Java Basics", fixture.title());
        assertEquals("Java Basics", observer.get(fixture.key()));
    }

    @Test
    void failedDatabaseWriteLeavesCacheUntouched() {
        assertThrows(DataIntegrityViolationException.class, () -> fixture.service.rename(1, "Spring Boot"));
        assertEquals("Java Basics", fixture.title());
        assertEquals("Java Basics", observer.get(fixture.key()));
    }

    @Test
    void unavailableInvalidationClientCannotRollBackCommittedData() {
        assertThrows(RuntimeException.class, () -> fixture.transactions.executeWithoutResult(status -> {
            fixture.service.rename(1, "Committed Despite Cache Failure");
            redis.close();
        }));
        assertEquals("Committed Despite Cache Failure", fixture.title());
        assertEquals("Java Basics", observer.get(fixture.key()));
    }
}

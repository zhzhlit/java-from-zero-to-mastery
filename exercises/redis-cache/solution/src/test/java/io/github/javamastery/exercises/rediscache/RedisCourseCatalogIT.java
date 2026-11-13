package io.github.javamastery.exercises.rediscache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisCourseCatalogIT {
    private final String namespace = "learning:redis:it:" + UUID.randomUUID() + ":";
    private final InMemoryCourseRepository source = new InMemoryCourseRepository();
    private JedisPooled firstClient;
    private JedisPooled secondClient;
    private RedisCourseCatalog first;
    private RedisCourseCatalog second;

    @BeforeEach
    void connectToDedicatedRedis() {
        String host = System.getProperty("redis.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("redis.port", "6379"));
        firstClient = new JedisPooled(host, port);
        secondClient = new JedisPooled(host, port);
        assertEquals("PONG", firstClient.ping());
        first = new RedisCourseCatalog(source, firstClient, namespace, Duration.ofMinutes(1));
        second = new RedisCourseCatalog(source, secondClient, namespace, Duration.ofMinutes(1));
    }

    @AfterEach
    void cleanOnlyOwnKeysAndCloseConnections() {
        try {
            if (firstClient != null) {
                firstClient.del(namespace + "course:1", namespace + "course:99", namespace + "other:course:1");
            }
        } finally {
            if (secondClient != null) {
                secondClient.close();
            }
            if (firstClient != null) {
                firstClient.close();
            }
        }
    }

    @Test
    void independentClientsShareCachedValue() {
        assertEquals(first.findById(1), second.findById(1));
        assertEquals(1, source.queryCount());
    }

    @Test
    void writeByOneClientInvalidatesForOtherClient() {
        second.findById(1);
        first.rename(1, "Shared Update");
        assertFalse(firstClient.exists(namespace + "course:1"));
        assertEquals("Shared Update", second.findById(1).orElseThrow().title());
        assertEquals(2, source.queryCount());
    }

    @Test
    void missingValuesAreNotCached() {
        assertTrue(first.findById(99).isEmpty());
        assertTrue(second.findById(99).isEmpty());
        assertFalse(firstClient.exists(namespace + "course:99"));
        assertEquals(2, source.queryCount());
    }

    @Test
    void valueHasTtlAndReloadsAfterServerExpiration() {
        first.findById(1);
        long remaining = firstClient.pttl(namespace + "course:1");
        assertTrue(remaining > 0 && remaining <= 60000);
        source.rename(1, "Source Changed");
        assertEquals("Java Basics", second.findById(1).orElseThrow().title());
        assertEquals(1, firstClient.pexpire(namespace + "course:1", 0));
        assertEquals("Source Changed", second.findById(1).orElseThrow().title());
        assertEquals(2, source.queryCount());
    }

    @Test
    void namespacesDoNotShareValues() {
        first.findById(1);
        source.rename(1, "Other Namespace");
        RedisCourseCatalog other = new RedisCourseCatalog(source, secondClient, namespace + "other:", Duration.ofMinutes(1));
        assertEquals("Other Namespace", other.findById(1).orElseThrow().title());
        assertEquals("Java Basics", first.findById(1).orElseThrow().title());
    }

    @Test
    void failedSourceWriteLeavesCachedValue() {
        first.findById(1);
        assertThrows(NoSuchElementException.class, () -> first.rename(99, "Missing"));
        assertEquals("Java Basics", firstClient.get(namespace + "course:1"));
        assertEquals(1, source.queryCount());
    }
}

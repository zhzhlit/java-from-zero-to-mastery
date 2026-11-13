package io.github.javamastery.exercises.rediscache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Disabled("练习：完成 RedisCourseCatalog 后移除 @Disabled")
class RedisCourseCatalogTest {
    private final JedisPooled redis = mock(JedisPooled.class);
    private final CourseRepository source = mock(CourseRepository.class);
    private final RedisCourseCatalog catalog = new RedisCourseCatalog(source, redis, "test:v1:", Duration.ofSeconds(10));

    @Test
    void hitDoesNotReadSource() {
        when(redis.get("test:v1:course:1")).thenReturn("Cached Title");
        assertEquals("Cached Title", catalog.findById(1).orElseThrow().title());
        verifyNoInteractions(source);
    }

    @Test
    void missLoadsAndWritesValueWithExpiration() {
        when(source.findById(1)).thenReturn(Optional.of(new Course(1, "Loaded")));
        assertEquals("Loaded", catalog.findById(1).orElseThrow().title());
        verify(redis).set(eq("test:v1:course:1"), eq("Loaded"), any(SetParams.class));
    }

    @Test
    void missingSourceValueDoesNotCreateCacheEntry() {
        when(source.findById(99)).thenReturn(Optional.empty());
        assertTrue(catalog.findById(99).isEmpty());
        verify(redis, never()).set(anyString(), anyString(), any(SetParams.class));
    }

    @Test
    void sourceFailurePropagatesWithoutCaching() {
        when(source.findById(1)).thenThrow(new IllegalStateException("source unavailable"));
        assertThrows(IllegalStateException.class, () -> catalog.findById(1));
        verify(redis, never()).set(anyString(), anyString(), any(SetParams.class));
    }

    @Test
    void redisReadFailureDoesNotSilentlyBypassCache() {
        when(redis.get("test:v1:course:1")).thenThrow(new JedisConnectionException("redis unavailable"));
        assertThrows(JedisConnectionException.class, () -> catalog.findById(1));
        verifyNoInteractions(source);
    }

    @Test
    void successfulWritePrecedesInvalidation() {
        when(source.rename(1, "New")).thenReturn(new Course(1, "New"));
        assertEquals("New", catalog.rename(1, "New").title());
        var order = inOrder(source, redis);
        order.verify(source).rename(1, "New");
        order.verify(redis).del("test:v1:course:1");
    }

    @Test
    void failedWriteDoesNotInvalidate() {
        when(source.rename(1, "")).thenThrow(new IllegalArgumentException("empty title"));
        assertThrows(IllegalArgumentException.class, () -> catalog.rename(1, ""));
        verifyNoInteractions(redis);
    }

    @Test
    void invalidationFailureIsVisibleAfterSourceWrite() {
        when(source.rename(1, "New")).thenReturn(new Course(1, "New"));
        doThrow(new JedisConnectionException("redis unavailable")).when(redis).del("test:v1:course:1");
        assertThrows(JedisConnectionException.class, () -> catalog.rename(1, "New"));
        verify(source).rename(1, "New");
    }

    @Test
    void explicitInvalidationUsesOnlyNamespacedKey() {
        catalog.invalidate(2);
        verify(redis).del("test:v1:course:2");
        verifyNoInteractions(source);
    }

    @Test
    void rejectsInvalidNamespaceTtlAndId() {
        for (String namespace : new String[]{"", "missing-colon", "bad space:", "bad\n:", "a".repeat(122) + ":"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RedisCourseCatalog(source, redis, namespace, Duration.ofSeconds(1)));
        }
        assertThrows(IllegalArgumentException.class, () -> new RedisCourseCatalog(source, redis, "test:", Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new RedisCourseCatalog(source, redis, "test:", Duration.ofNanos(1)));
        assertThrows(IllegalArgumentException.class, () -> catalog.findById(0));
        assertThrows(IllegalArgumentException.class, () -> catalog.rename(-1, "Title"));
        assertThrows(IllegalArgumentException.class, () -> catalog.invalidate(-1));
        verifyNoInteractions(redis, source);
    }
}

package io.github.javamastery.exercises.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CachedCourseCatalogTest {
    private final InMemoryCourseRepository source = new InMemoryCourseRepository();
    private final AtomicLong nanos = new AtomicLong();
    private final CachedCourseCatalog catalog = new CachedCourseCatalog(source, 2, Duration.ofSeconds(10), nanos::get);

    @Test
    void cachesRepeatedReadsAndRecordsStatistics() {
        assertEquals(catalog.findById(1), catalog.findById(1));
        assertEquals(1, source.queryCount());
        assertEquals(1, catalog.stats().hitCount());
        assertEquals(1, catalog.stats().missCount());
    }

    @Test
    void isolatesKeys() {
        assertEquals(1, catalog.findById(1).orElseThrow().id());
        assertEquals(2, catalog.findById(2).orElseThrow().id());
        assertEquals(2, source.queryCount());
    }

    @Test
    void expiresAfterWriteUsingControlledTime() {
        catalog.findById(1);
        source.rename(1, "Changed Outside Cache");
        nanos.set(Duration.ofSeconds(9).toNanos());
        assertEquals("Java Basics", catalog.findById(1).orElseThrow().title());
        nanos.set(Duration.ofSeconds(10).toNanos());
        assertEquals("Changed Outside Cache", catalog.findById(1).orElseThrow().title());
        assertEquals(2, source.queryCount());
    }

    @Test
    void invalidatesAfterSuccessfulSourceWrite() {
        catalog.findById(1);
        catalog.rename(1, "  Java 21  ");
        assertEquals("Java 21", catalog.findById(1).orElseThrow().title());
        assertEquals(2, source.queryCount());
    }

    @Test
    void doesNotCacheAbsentValues() {
        assertTrue(catalog.findById(999).isEmpty());
        assertTrue(catalog.findById(999).isEmpty());
        assertEquals(2, source.queryCount());
    }

    @Test
    void retriesAfterSourceFailureInsteadOfCachingException() {
        AtomicBoolean failing = new AtomicBoolean(true);
        CourseRepository unreliable = new CourseRepository() {
            public Optional<Course> findById(long id) {
                if (failing.getAndSet(false)) {
                    throw new IllegalStateException("source unavailable");
                }
                return source.findById(id);
            }

            public Course rename(long id, String title) {
                return source.rename(id, title);
            }
        };
        CachedCourseCatalog retrying = new CachedCourseCatalog(unreliable, 2, Duration.ofSeconds(10));
        assertThrows(IllegalStateException.class, () -> retrying.findById(1));
        assertTrue(retrying.findById(1).isPresent());
    }

    @Test
    void boundsCapacityAfterMaintenance() {
        for (long id : new long[]{1, 2, 3}) {
            catalog.findById(id);
        }
        assertTrue(catalog.sizeAfterMaintenance() <= 2);
    }

    @Test
    void rejectsInvalidConfigurationAndKeys() {
        assertThrows(IllegalArgumentException.class, () -> new CachedCourseCatalog(source, 0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new CachedCourseCatalog(source, 1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new CachedCourseCatalog(source, 1, Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> catalog.findById(0));
        assertThrows(IllegalArgumentException.class, () -> catalog.invalidate(-1));
    }

    @Test
    void failedWriteLeavesExistingCachedValueUsable() {
        catalog.findById(1);
        assertThrows(IllegalArgumentException.class, () -> catalog.rename(1, " "));
        assertThrows(NoSuchElementException.class, () -> catalog.rename(999, "Missing"));
        assertEquals("Java Basics", catalog.findById(1).orElseThrow().title());
        assertEquals(1, source.queryCount());
    }

    @Test
    void explicitInvalidationLoadsAgain() {
        catalog.findById(1);
        catalog.invalidate(1);
        assertFalse(catalog.findById(1).isEmpty());
        assertEquals(2, source.queryCount());
    }

    @Test
    void concurrentReadsOfSameKeyShareLoadedValue() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            CountDownLatch ready = new CountDownLatch(8);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Optional<Course>>> futures = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("start timeout");
                    }
                    return catalog.findById(1);
                }));
            }
            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS));
            } finally {
                start.countDown();
            }
            for (Future<Optional<Course>> future : futures) {
                assertEquals(1, future.get(5, TimeUnit.SECONDS).orElseThrow().id());
            }
            assertEquals(1, source.queryCount());
        }
    }
}

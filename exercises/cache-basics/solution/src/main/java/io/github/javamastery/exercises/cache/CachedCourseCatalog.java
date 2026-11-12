package io.github.javamastery.exercises.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class CachedCourseCatalog {
    private final CourseRepository source;
    private final Cache<Long, Course> cache;

    public CachedCourseCatalog(CourseRepository source, long maximumSize, Duration ttl) {
        this(source, maximumSize, ttl, Ticker.systemTicker());
    }

    public CachedCourseCatalog(CourseRepository source, long maximumSize, Duration ttl, Ticker ticker) {
        this.source = Objects.requireNonNull(source);
        Objects.requireNonNull(ttl);
        Objects.requireNonNull(ticker);
        if (maximumSize <= 0 || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("缓存容量与 TTL 必须大于 0");
        }
        this.cache = Caffeine.newBuilder().maximumSize(maximumSize).expireAfterWrite(ttl)
                .ticker(ticker).recordStats().build();
    }

    public Optional<Course> findById(long id) {
        requirePositiveId(id);
        return Optional.ofNullable(cache.get(id, key -> source.findById(key).orElse(null)));
    }

    public Course rename(long id, String title) {
        requirePositiveId(id);
        Course updated = source.rename(id, title);
        cache.invalidate(id);
        return updated;
    }

    public void invalidate(long id) {
        requirePositiveId(id);
        cache.invalidate(id);
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public long sizeAfterMaintenance() {
        cache.cleanUp();
        return cache.estimatedSize();
    }

    private void requirePositiveId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须大于 0");
        }
    }
}

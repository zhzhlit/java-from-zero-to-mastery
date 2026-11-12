package io.github.javamastery.exercises.rediscache;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.SetParams;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class RedisCourseCatalog {
    private static final Pattern SAFE_NAMESPACE = Pattern.compile("[A-Za-z0-9:_-]{1,120}:");
    private final CourseRepository source;
    private final JedisPooled redis;
    private final String namespace;
    private final long ttlMillis;

    public RedisCourseCatalog(CourseRepository source, JedisPooled redis, String namespace, Duration ttl) {
        this.source = Objects.requireNonNull(source);
        this.redis = Objects.requireNonNull(redis);
        this.namespace = Objects.requireNonNull(namespace);
        Objects.requireNonNull(ttl);
        if (!SAFE_NAMESPACE.matcher(namespace).matches() || ttl.isNegative() || ttl.toMillis() <= 0) {
            throw new IllegalArgumentException("命名空间必须为安全字符并以冒号结尾，TTL 至少为 1 毫秒");
        }
        this.ttlMillis = ttl.toMillis();
    }

    public Optional<Course> findById(long id) {
        String key = key(id);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            return source.findById(id);
        }
        String cachedTitle = redis.get(key);
        if (cachedTitle != null) {
            return Optional.of(new Course(id, cachedTitle));
        }
        Optional<Course> course = source.findById(id);
        course.ifPresent(value -> redis.set(key, value.title(), SetParams.setParams().px(ttlMillis)));
        return course;
    }

    public Course rename(long id, String title) {
        String key = key(id);
        Course updated = source.rename(id, title);
        redis.del(key);
        return updated;
    }

    public void invalidate(long id) {
        redis.del(key(id));
    }

    private String key(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须大于 0");
        }
        return namespace + "course:" + id;
    }
}

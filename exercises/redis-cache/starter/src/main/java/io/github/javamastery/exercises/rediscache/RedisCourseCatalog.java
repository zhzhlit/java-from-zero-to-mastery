package io.github.javamastery.exercises.rediscache;

import redis.clients.jedis.JedisPooled;

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
        throw new UnsupportedOperationException("练习：GET 命中返回，未命中读源并用 SET PX 一次写入值和 TTL");
    }

    public Course rename(long id, String title) {
        throw new UnsupportedOperationException("练习：成功写源后 DEL 对应键，失败必须传播");
    }

    public void invalidate(long id) {
        throw new UnsupportedOperationException("练习：校验 ID 并删除命名空间内的单个键");
    }

    private String key(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须大于 0");
        }
        return namespace + "course:" + id;
    }
}

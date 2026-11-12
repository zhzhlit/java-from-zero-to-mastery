package io.github.javamastery.exercises.rediscache;

import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.UUID;

public class RedisCacheDemo {
    public static void main(String[] args) {
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String namespace = "learning:redis:demo:" + UUID.randomUUID() + ":";
        InMemoryCourseRepository source = new InMemoryCourseRepository();
        try (JedisPooled firstClient = new JedisPooled(host, port);
             JedisPooled secondClient = new JedisPooled(host, port)) {
            RedisCourseCatalog first = new RedisCourseCatalog(source, firstClient, namespace, Duration.ofMinutes(1));
            RedisCourseCatalog second = new RedisCourseCatalog(source, secondClient, namespace, Duration.ofMinutes(1));
            try {
                System.out.println(first.findById(1).orElseThrow());
                System.out.println(second.findById(1).orElseThrow());
                System.out.println("Source queries across two clients: " + source.queryCount());
                first.rename(1, "Shared Redis Cache");
                System.out.println(second.findById(1).orElseThrow());
                System.out.println("Source queries after shared invalidation: " + source.queryCount());
            } finally {
                first.invalidate(1);
            }
        }
    }
}

package io.github.javamastery.exercises.rediscache;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

public class OutboxRecoveryDemo {
    public static void main(String[] args) throws IOException {
        Path directory = Files.createTempDirectory("java-outbox-demo-");
        String url = "jdbc:h2:file:" + directory.resolve("courses") + ";DB_CLOSE_DELAY=-1";
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("transaction-schema.sql"),
                new ClassPathResource("outbox-schema.sql")).execute(dataSource);
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String namespace = "learning:outbox:demo:" + UUID.randomUUID() + ":";
        try (JedisPooled redis = new JedisPooled(host, port)) {
            JdbcCourseRepository source = new JdbcCourseRepository(jdbc);
            RedisCourseCatalog catalog = new RedisCourseCatalog(source, redis, namespace, Duration.ofMinutes(1));
            try {
                catalog.findById(1);
                OutboxCourseService service = new OutboxCourseService(source, new InvalidationOutbox(jdbc), catalog,
                        new TransactionTemplate(new DataSourceTransactionManager(dataSource)));
                service.rename(1, "Recovered Title");
                System.out.println("Before recovery, cache: " + redis.get(namespace + "course:1"));
                jdbc.execute("SHUTDOWN");
                JdbcTemplate reopened = new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
                InvalidationOutbox recovered = new InvalidationOutbox(reopened);
                System.out.println("Pending tasks after database reopen: " + recovered.pendingBatch(10).size());
                OutboxDispatcher.DispatchResult result = new OutboxDispatcher(redis, recovered).dispatchBatch(10);
                if (!result.failures().isEmpty()) {
                    throw new IllegalStateException("失效处理失败", result.failures().getFirst().cause());
                }
                System.out.println("Delivered tasks: " + result.delivered());
                System.out.println("After recovery: " + catalog.findById(1).orElseThrow().title());
            } finally {
                catalog.invalidate(1);
            }
        } finally {
            jdbc.execute("SHUTDOWN");
            Files.deleteIfExists(directory.resolve("courses.mv.db"));
            Files.deleteIfExists(directory.resolve("courses.trace.db"));
            Files.deleteIfExists(directory);
        }
    }
}

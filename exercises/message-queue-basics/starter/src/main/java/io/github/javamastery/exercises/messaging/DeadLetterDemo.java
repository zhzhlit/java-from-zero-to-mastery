package io.github.javamastery.exercises.messaging;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

import java.util.Map;
import java.util.UUID;

public class DeadLetterDemo {
    public static void main(String[] args) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("inbox-schema.sql")).execute(dataSource);
        String stream = "learning:mq:dead-demo:" + UUID.randomUUID();
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        try (JedisPooled redis = new JedisPooled(host, port)) {
            CourseEventQueue queue = new CourseEventQueue(redis, stream, "notifications");
            StreamFailurePolicy policy = new StreamFailurePolicy(redis, queue, 3);
            try {
                queue.ensureGroup();
                redis.xadd(stream, StreamEntryID.NEW_ENTRY, Map.of("schemaVersion", "unsupported"));
                queue.publish(new CoursePublishedEvent(UUID.randomUUID(), 1, "Healthy Event"));
                NotificationInbox inbox = new NotificationInbox(jdbc,
                        new TransactionTemplate(new DataSourceTransactionManager(dataSource)), "notifications");
                var result = new ResilientNotificationConsumer(queue, inbox, policy)
                        .processBatch("worker", queue.readNew("worker", 10));
                System.out.println("Applied notifications: " + result.applied());
                System.out.println("Dead-lettered messages: " + result.deadLettered());
                System.out.println("Dead-letter stream length: " + redis.xlen(policy.deadLetterStream()));
                System.out.println("Pending after processing: " + queue.pendingCount());
            } finally {
                redis.del(stream, policy.failureStateKey(), policy.deadLetterStream());
            }
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

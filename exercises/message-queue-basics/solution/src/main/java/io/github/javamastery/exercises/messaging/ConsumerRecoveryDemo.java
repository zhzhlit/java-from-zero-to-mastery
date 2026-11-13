package io.github.javamastery.exercises.messaging;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;

import java.time.Duration;
import java.util.UUID;

public class ConsumerRecoveryDemo {
    public static void main(String[] args) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("inbox-schema.sql")).execute(dataSource);
        String stream = "learning:mq:recovery:" + UUID.randomUUID();
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        try (JedisPooled redis = new JedisPooled(host, port)) {
            try {
                CourseEventQueue queue = new CourseEventQueue(redis, stream, "notifications");
                queue.ensureGroup();
                CoursePublishedEvent event = new CoursePublishedEvent(UUID.randomUUID(), 1, "Inbox Recovery");
                queue.publish(event);
                queue.readNew("original", 1);
                NotificationInbox inbox = new NotificationInbox(jdbc,
                        new TransactionTemplate(new DataSourceTransactionManager(dataSource)), "notifications");
                inbox.storeIfNew(event);
                // Simulate the gap after a business commit but before XACK.
                ClaimBatch recovered = queue.claimIdle("replacement", Duration.ZERO, new StreamEntryID("0-0"), 10);
                var result = new NotificationConsumer(queue, inbox).processBatch(recovered.messages());
                System.out.println("Recovered messages: " + recovered.messages().size());
                System.out.println("New effects during replay: " + result.applied());
                System.out.println("Duplicates during replay: " + result.duplicates());
                System.out.println("Stored notifications: " + jdbc.queryForObject("SELECT COUNT(*) FROM course_notifications", Integer.class));
                System.out.println("Pending after recovery: " + queue.pendingCount());
            } finally {
                redis.del(stream);
            }
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

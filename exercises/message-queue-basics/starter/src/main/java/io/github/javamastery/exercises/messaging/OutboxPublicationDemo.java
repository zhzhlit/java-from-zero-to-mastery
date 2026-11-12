package io.github.javamastery.exercises.messaging;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public class OutboxPublicationDemo {
    public static void main(String[] args) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("publication-schema.sql"),
                new ClassPathResource("inbox-schema.sql")).execute(dataSource);
        TransactionTemplate transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        String stream = "learning:mq:publication-demo:" + UUID.randomUUID();
        PublicationOutbox outbox = new PublicationOutbox(jdbc, stream);
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        try (JedisPooled redis = new JedisPooled(host, port)) {
            try {
                new CoursePublishingService(jdbc, outbox, transactions).publish(1, "Reliable Publication");
                System.out.println("Pending before relay: " + outbox.pendingBatch(10).size());
                CourseEventQueue queue = new CourseEventQueue(redis, stream, "notifications");
                queue.ensureGroup();
                System.out.println("Published messages: " + new PublicationRelay(outbox, queue).relayBatch(10).published());
                NotificationInbox inbox = new NotificationInbox(jdbc, transactions, "notifications");
                var result = new NotificationConsumer(queue, inbox).processBatch(queue.readNew("worker", 10));
                System.out.println("Applied notifications: " + result.applied());
                System.out.println("Pending outbox: " + outbox.pendingBatch(10).size());
                System.out.println("Pending messages: " + queue.pendingCount());
            } finally {
                redis.del(stream);
            }
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

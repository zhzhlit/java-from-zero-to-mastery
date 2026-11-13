package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.messaging.PublicationRelay;
import io.github.javamastery.exercises.search.CourseDocument;
import io.github.javamastery.exercises.search.CourseLevel;
import io.github.javamastery.exercises.search.CourseSearchIndex;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public class MiddlewareReviewDemo {
    public static void main(String[] args) throws Exception {
        var dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("publication-schema.sql"),
                new ClassPathResource("inbox-schema.sql"), new ClassPathResource("review-course-schema.sql")).execute(dataSource);
        String stream = "learning:review:demo:" + UUID.randomUUID();
        var source = new ReviewCourseCatalog(jdbc, new TransactionTemplate(new DataSourceTransactionManager(dataSource)), stream);
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        try (JedisPooled redis = new JedisPooled(host, port); var directory = new ByteBuffersDirectory();
             var index = new CourseSearchIndex(directory); var rpc = new ReviewRpcEndpoint(source)) {
            try {
                var event = source.release(new CourseDocument(1, "Java 综合实践", "Redis Lucene gRPC", CourseLevel.INTERMEDIATE));
                System.out.println("RPC before projection: " + rpc.client().getCourse(1).title());
                System.out.println("Search before projection: " + index.search("java", null, 10).totalHits());
                var workflow = new CoursePublicationWorkflow(source, index, redis, "review");
                workflow.queue().ensureGroup();
                new PublicationRelay(source.outbox(), workflow.queue()).relayBatch(10);
                workflow.processBatch(workflow.queue().readNew("worker", 10));
                System.out.println("Search after projection: " + index.search("java", null, 10).totalHits());
                workflow.queue().publish(event);
                var replay = workflow.processBatch(workflow.queue().readNew("worker", 10));
                System.out.println("New notifications on replay: " + replay.newNotifications());
                System.out.println("Pending messages: " + workflow.queue().pendingCount());
                System.out.println("Pending outbox: " + source.outbox().pendingBatch(10).size());
            } finally {
                redis.del(stream);
            }
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

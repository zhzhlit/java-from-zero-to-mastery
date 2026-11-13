package io.github.javamastery.exercises.rediscache;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.UUID;

public class TransactionCacheDemo {
    public static void main(String[] args) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("transaction-schema.sql")).execute(dataSource);
        String host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String namespace = "learning:tx:demo:" + UUID.randomUUID() + ":";
        try (JedisPooled redis = new JedisPooled(host, port)) {
            JdbcCourseRepository source = new JdbcCourseRepository(jdbc);
            RedisCourseCatalog catalog = new RedisCourseCatalog(source, redis, namespace, Duration.ofMinutes(1));
            TransactionTemplate transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
            TransactionalCourseService service = new TransactionalCourseService(source,
                    new AfterCommitInvalidator(catalog), transactions);
            try {
                catalog.findById(1);
                transactions.executeWithoutResult(status -> {
                    service.rename(1, "Committed Title");
                    System.out.println("Before outer commit, shared cache: " + redis.get(namespace + "course:1"));
                });
                System.out.println("After commit: " + catalog.findById(1).orElseThrow().title());
                transactions.executeWithoutResult(status -> {
                    service.rename(1, "Rolled Back Title");
                    status.setRollbackOnly();
                });
                System.out.println("After rollback: " + catalog.findById(1).orElseThrow().title());
            } finally {
                catalog.invalidate(1);
            }
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

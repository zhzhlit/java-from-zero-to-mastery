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

class TransactionFixture implements AutoCloseable {
    final String namespace = "learning:tx:it:" + UUID.randomUUID() + ":";
    final JdbcTemplate jdbc;
    final JdbcCourseRepository source;
    final RedisCourseCatalog catalog;
    final AfterCommitInvalidator invalidator;
    final TransactionTemplate transactions;
    final TransactionalCourseService service;

    TransactionFixture(JedisPooled redis) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("transaction-schema.sql")).execute(dataSource);
        source = new JdbcCourseRepository(jdbc);
        catalog = new RedisCourseCatalog(source, redis, namespace, Duration.ofMinutes(1));
        invalidator = new AfterCommitInvalidator(catalog);
        transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        service = new TransactionalCourseService(source, invalidator, transactions);
    }

    String key() {
        return namespace + "course:1";
    }

    String title() {
        return source.findById(1).orElseThrow().title();
    }

    @Override
    public void close() {
        jdbc.execute("SHUTDOWN");
    }
}

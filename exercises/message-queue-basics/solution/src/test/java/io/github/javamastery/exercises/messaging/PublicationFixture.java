package io.github.javamastery.exercises.messaging;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

class PublicationFixture implements AutoCloseable {
    final JdbcTemplate jdbc;
    final TransactionTemplate transactions;
    final PublicationOutbox outbox;
    final CoursePublishingService service;

    PublicationFixture(String stream) {
        this(stream, "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    }

    PublicationFixture(String stream, String url) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("publication-schema.sql"),
                new ClassPathResource("inbox-schema.sql")).execute(dataSource);
        transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        outbox = new PublicationOutbox(jdbc, stream);
        service = new CoursePublishingService(jdbc, outbox, transactions);
    }

    int count(String table) {
        String sql = switch (table) {
            case "courses" -> "SELECT COUNT(*) FROM published_courses";
            case "outbox" -> "SELECT COUNT(*) FROM publication_outbox";
            case "notifications" -> "SELECT COUNT(*) FROM course_notifications";
            default -> throw new IllegalArgumentException("未知测试表");
        };
        return jdbc.queryForObject(sql, Integer.class);
    }

    @Override
    public void close() {
        jdbc.execute("SHUTDOWN");
    }
}

package io.github.javamastery.exercises.messaging;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

class InboxFixture implements AutoCloseable {
    final JdbcTemplate jdbc;
    final TransactionTemplate transactions;
    final NotificationInbox inbox;

    InboxFixture() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("inbox-schema.sql")).execute(dataSource);
        transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        inbox = new NotificationInbox(jdbc, transactions, "notifications");
    }

    int notifications() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM course_notifications", Integer.class);
    }

    int receipts() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM inbox_receipts", Integer.class);
    }

    @Override
    public void close() {
        jdbc.execute("SHUTDOWN");
    }
}

package io.github.javamastery.exercises.middleware;

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

import java.io.IOException;
import java.util.UUID;

class ReviewFixture implements AutoCloseable {
    final JdbcTemplate jdbc;
    final TransactionTemplate transactions;
    final ReviewCourseCatalog source;
    final ByteBuffersDirectory directory = new ByteBuffersDirectory();
    final CourseSearchIndex index;

    ReviewFixture(String stream) throws IOException {
        var dataSource = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("publication-schema.sql"),
                new ClassPathResource("inbox-schema.sql"), new ClassPathResource("review-course-schema.sql")).execute(dataSource);
        transactions = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        source = new ReviewCourseCatalog(jdbc, transactions, stream);
        index = new CourseSearchIndex(directory);
    }

    CourseDocument course(long id) {
        return new CourseDocument(id, "Java 综合实践 " + id, "Redis Lucene RPC", CourseLevel.INTERMEDIATE);
    }

    int notifications() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM course_notifications", Integer.class);
    }

    int receipts() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM inbox_receipts", Integer.class);
    }

    @Override
    public void close() throws IOException {
        try (var directoryToClose = directory; var indexToClose = index) {
        } finally {
            jdbc.execute("SHUTDOWN");
        }
    }
}

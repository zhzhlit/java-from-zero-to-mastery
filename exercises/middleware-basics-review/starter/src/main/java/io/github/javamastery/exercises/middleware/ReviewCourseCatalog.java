package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.messaging.CoursePublishedEvent;
import io.github.javamastery.exercises.messaging.PublicationOutbox;
import io.github.javamastery.exercises.rpc.Course;
import io.github.javamastery.exercises.rpc.CourseCatalog;
import io.github.javamastery.exercises.search.CourseDocument;
import io.github.javamastery.exercises.search.CourseLevel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReviewCourseCatalog implements CourseCatalog {
    private static final String SELECT = "SELECT c.id, c.title, d.description, d.course_level FROM published_courses c "
            + "JOIN review_course_details d ON d.course_id = c.id";
    private static final RowMapper<CourseDocument> MAPPER = (rs, row) -> new CourseDocument(rs.getLong("id"),
            rs.getString("title"), rs.getString("description"), CourseLevel.valueOf(rs.getString("course_level")));
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final PublicationOutbox outbox;
    private final String stream;

    public ReviewCourseCatalog(JdbcTemplate jdbc, TransactionTemplate transactions, String stream) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.transactions = Objects.requireNonNull(transactions);
        this.outbox = new PublicationOutbox(jdbc, stream);
        this.stream = stream;
    }

    public CoursePublishedEvent release(CourseDocument course) {
        throw new UnsupportedOperationException("练习：在实际事务中写入课程、详情和发布事件");
    }

    public Optional<CourseDocument> findDocument(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须为正数");
        }
        return jdbc.query(SELECT + " WHERE c.id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<Course> findById(long id) {
        return findDocument(id).map(course -> new Course(course.id(), course.title()));
    }

    public PublicationOutbox outbox() {
        return outbox;
    }

    List<CourseDocument> allDocuments() {
        return jdbc.query(SELECT + " ORDER BY c.id", MAPPER);
    }

    JdbcTemplate jdbc() {
        return jdbc;
    }

    TransactionTemplate transactions() {
        return transactions;
    }

    String streamName() {
        return stream;
    }

    static void requireOutsideTransaction() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("该操作必须在外层业务事务之外执行");
        }
    }
}

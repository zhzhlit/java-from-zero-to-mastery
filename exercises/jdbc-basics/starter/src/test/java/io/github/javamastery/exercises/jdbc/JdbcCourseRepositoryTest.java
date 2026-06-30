package io.github.javamastery.exercises.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JdbcCourseRepository")
class JdbcCourseRepositoryTest {
    private static final String URL = "jdbc:h2:mem:jdbc_basics_starter;DB_CLOSE_DELAY=-1";
    private JdbcCourseRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new JdbcCourseRepository(new ConnectionFactory(new DatabaseConfig(URL, "sa", "")));
        try (Connection connection = DriverManager.getConnection(URL, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS courses");
            statement.executeUpdate("""
                    CREATE TABLE courses (
                        id BIGINT PRIMARY KEY,
                        title VARCHAR(100) NOT NULL,
                        lesson_count INT NOT NULL,
                        published BOOLEAN NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    INSERT INTO courses (id, title, lesson_count, published) VALUES
                    (1, 'Java 基础', 8, TRUE),
                    (2, '数据库基础', 6, TRUE),
                    (3, '草稿课程', 3, FALSE)
                    """);
        }
    }

    @Test
    @Disabled("练习：移除 @Disabled，按 id 查询并映射 ResultSet")
    @DisplayName("按 id 查询课程并映射 ResultSet")
    void findsCourseById() {
        Optional<CourseRecord> course = repository.findById(2);

        assertTrue(course.isPresent());
        assertEquals(new CourseRecord(2, "数据库基础", 6, true), course.orElseThrow());
    }

    @Test
    @Disabled("练习：移除 @Disabled，找不到记录时返回 empty")
    @DisplayName("找不到记录时返回 empty")
    void returnsEmptyWhenCourseIsMissing() {
        assertTrue(repository.findById(99).isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，用参数化查询筛选课程")
    @DisplayName("用参数化查询筛选已发布课程")
    void findsPublishedCoursesByMinimumLessons() {
        List<CourseRecord> courses = repository.findPublished(7);

        assertEquals(List.of(new CourseRecord(1, "Java 基础", 8, true)), courses);
    }

    @Test
    @Disabled("练习：移除 @Disabled，使用 PreparedStatement 插入课程")
    @DisplayName("使用 PreparedStatement 插入课程")
    void createsCourse() {
        int updatedRows = repository.create(new CourseRecord(4, "JDBC 基础", 5, true));

        assertEquals(1, updatedRows);
        assertEquals(new CourseRecord(4, "JDBC 基础", 5, true), repository.findById(4).orElseThrow());
    }
}

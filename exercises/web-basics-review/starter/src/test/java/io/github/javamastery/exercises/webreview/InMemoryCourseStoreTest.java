package io.github.javamastery.exercises.webreview;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InMemoryCourseStore")
class InMemoryCourseStoreTest {
    @Test
    @Disabled("练习：移除 @Disabled，完成对应实现")
    @DisplayName("只列出已发布课程并按 id 排序")
    void listsPublishedCoursesInIdOrder() {
        InMemoryCourseStore store = new InMemoryCourseStore(List.of(
                new CourseSummary(2, "JDBC", 8, true),
                new CourseSummary(1, "Draft", 3, false),
                new CourseSummary(3, "HTTP", 6, true)
        ));

        assertEquals(List.of("JDBC", "HTTP"), store.publishedCourses().stream().map(CourseSummary::title).toList());
    }

    @Test
    @Disabled("练习：移除 @Disabled，完成对应实现")
    @DisplayName("创建课程时分配下一个 id 且默认未发布")
    void createsDraftCourseWithNextId() {
        InMemoryCourseStore store = new InMemoryCourseStore(List.of(new CourseSummary(5, "HTTP", 6, true)));

        CourseSummary created = store.create("Servlet", 7);

        assertEquals(6, created.id());
        assertEquals("Servlet", created.title());
        assertTrue(store.findById(6).isPresent());
    }
}

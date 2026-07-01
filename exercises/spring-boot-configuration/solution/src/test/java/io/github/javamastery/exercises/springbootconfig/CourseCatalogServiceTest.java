package io.github.javamastery.exercises.springbootconfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CourseCatalogService")
class CourseCatalogServiceTest {
    private final CourseCatalogService service = new CourseCatalogService(
            new CourseCatalogProperties("默认课程", 8, List.of("java", "spring"))
    );

    @Test
    @DisplayName("空关键词使用配置中的默认标题")
    void usesDefaultTitleWhenKeywordIsBlank() {
        CourseSearchView view = service.buildSearchView(" ", 5);

        assertEquals("默认课程", view.keyword());
        assertEquals(5, view.pageSize());
    }

    @Test
    @DisplayName("请求页大小不能超过配置上限")
    void capsPageSizeByConfiguration() {
        CourseSearchView view = service.buildSearchView("  REST API  ", 100);

        assertEquals("REST API", view.keyword());
        assertEquals(8, view.pageSize());
    }

    @Test
    @DisplayName("特色标签匹配忽略大小写和首尾空格")
    void matchesFeaturedTags() {
        assertTrue(service.isFeaturedTag(" Spring "));
        assertFalse(service.isFeaturedTag("database"));
    }
}

package io.github.javamastery.exercises.springbootconfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CourseCatalogProperties")
class CourseCatalogPropertiesTest {
    @Test
    @DisplayName("从 Environment 绑定 course.catalog 配置")
    void bindsFromEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("course.catalog.default-title", "企业级 Java")
                .withProperty("course.catalog.page-size", "12")
                .withProperty("course.catalog.featured-tags[0]", "spring")
                .withProperty("course.catalog.featured-tags[1]", "config");

        CourseCatalogProperties properties = Binder.get(environment)
                .bind("course.catalog", CourseCatalogProperties.class)
                .get();

        assertEquals("企业级 Java", properties.defaultTitle());
        assertEquals(12, properties.pageSize());
        assertEquals(List.of("spring", "config"), properties.featuredTags());
    }

    @Test
    @DisplayName("缺省或非法配置使用安全默认值")
    void usesSafeDefaults() {
        CourseCatalogProperties properties = new CourseCatalogProperties(" ", 0, null);

        assertEquals("Java 学习路线", properties.defaultTitle());
        assertEquals(10, properties.pageSize());
        assertEquals(List.of(), properties.featuredTags());
    }
}

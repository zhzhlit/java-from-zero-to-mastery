package io.github.javamastery.exercises.springbootconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("course.catalog")
public record CourseCatalogProperties(String defaultTitle, int pageSize, List<String> featuredTags) {
    public CourseCatalogProperties {
        // TODO: 处理空标题、非法 pageSize 和 null featuredTags。
    }
}

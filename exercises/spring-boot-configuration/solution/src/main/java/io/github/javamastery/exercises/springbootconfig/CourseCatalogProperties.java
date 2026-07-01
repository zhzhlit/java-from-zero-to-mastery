package io.github.javamastery.exercises.springbootconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("course.catalog")
public record CourseCatalogProperties(String defaultTitle, int pageSize, List<String> featuredTags) {
    public CourseCatalogProperties {
        if (defaultTitle == null || defaultTitle.isBlank()) {
            defaultTitle = "Java 学习路线";
        } else {
            defaultTitle = defaultTitle.trim();
        }

        if (pageSize <= 0) {
            pageSize = 10;
        }

        featuredTags = featuredTags == null ? List.of() : List.copyOf(featuredTags);
    }
}

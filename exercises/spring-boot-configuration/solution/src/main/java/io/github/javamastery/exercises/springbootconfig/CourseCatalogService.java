package io.github.javamastery.exercises.springbootconfig;

import org.springframework.stereotype.Service;

@Service
public class CourseCatalogService {
    private final CourseCatalogProperties properties;

    public CourseCatalogService(CourseCatalogProperties properties) {
        this.properties = properties;
    }

    public CourseSearchView buildSearchView(String keyword, int requestedPageSize) {
        String normalizedKeyword = keyword == null || keyword.isBlank()
                ? properties.defaultTitle()
                : keyword.trim();
        int pageSize = requestedPageSize <= 0
                ? properties.pageSize()
                : Math.min(requestedPageSize, properties.pageSize());
        return new CourseSearchView(normalizedKeyword, pageSize, properties.featuredTags());
    }

    public boolean isFeaturedTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }
        String normalizedTag = tag.trim();
        return properties.featuredTags().stream().anyMatch(featured -> featured.equalsIgnoreCase(normalizedTag));
    }
}

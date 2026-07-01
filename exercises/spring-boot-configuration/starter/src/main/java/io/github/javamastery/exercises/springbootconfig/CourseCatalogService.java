package io.github.javamastery.exercises.springbootconfig;

import org.springframework.stereotype.Service;

@Service
public class CourseCatalogService {
    private final CourseCatalogProperties properties;

    public CourseCatalogService(CourseCatalogProperties properties) {
        this.properties = properties;
    }

    public CourseSearchView buildSearchView(String keyword, int requestedPageSize) {
        return new CourseSearchView(keyword, requestedPageSize, properties.featuredTags());
    }

    public boolean isFeaturedTag(String tag) {
        return false;
    }
}

package io.github.javamastery.exercises.springbootconfig;

import java.util.List;

public record CourseSearchView(String keyword, int pageSize, List<String> featuredTags) {
}

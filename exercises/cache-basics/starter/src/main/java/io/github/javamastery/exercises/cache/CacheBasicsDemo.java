package io.github.javamastery.exercises.cache;

import java.time.Duration;

public class CacheBasicsDemo {
    public static void main(String[] args) {
        InMemoryCourseRepository source = new InMemoryCourseRepository();
        CachedCourseCatalog catalog = new CachedCourseCatalog(source, 100, Duration.ofMinutes(1));
        System.out.println(catalog.findById(1).orElseThrow());
        System.out.println(catalog.findById(1).orElseThrow());
        System.out.println("Source queries after two reads: " + source.queryCount());
        catalog.rename(1, "Java 21 Basics");
        System.out.println(catalog.findById(1).orElseThrow());
        System.out.println("Source queries after rename: " + source.queryCount());
        System.out.println("Cache hits: " + catalog.stats().hitCount());
    }
}

package io.github.javamastery.exercises.search;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class SearchBasicsDemo {
    public static void main(String[] args) throws Exception {
        Path path = Files.createTempDirectory("java-mastery-search-");
        try {
            try (Directory directory = FSDirectory.open(path); CourseSearchIndex index = new CourseSearchIndex(directory)) {
                index.upsert(new CourseDocument(1, "Java 缓存基础", "Learn Redis cache", CourseLevel.BEGINNER));
                index.upsert(new CourseDocument(2, "Java 并发进阶", "Virtual threads and locks", CourseLevel.ADVANCED));
                System.out.println("Before refresh: " + index.search("java", null, 10).totalHits());
                index.refresh();
                System.out.println("After refresh: " + index.search("java", null, 10).totalHits());
                System.out.println("Chinese cache matches: " + index.search("缓存", null, 10).totalHits());
                System.out.println("Beginner matches: " + index.search("java", CourseLevel.BEGINNER, 10).totalHits());
                index.commit();
            }
            try (Directory directory = FSDirectory.open(path); CourseSearchIndex index = new CourseSearchIndex(directory)) {
                System.out.println("Reopened matches: " + index.search("java", null, 10).totalHits());
            }
        } finally {
            try (var files = Files.walk(path)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(file);
                }
            }
        }
    }
}

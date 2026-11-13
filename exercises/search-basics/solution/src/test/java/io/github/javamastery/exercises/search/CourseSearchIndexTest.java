package io.github.javamastery.exercises.search;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseSearchIndexTest {
    private Directory directory;
    private CourseSearchIndex index;

    @BeforeEach
    void createIndex() throws IOException {
        directory = new ByteBuffersDirectory();
        index = new CourseSearchIndex(directory);
    }

    @AfterEach
    void closeResources() throws IOException {
        try (Directory directoryToClose = directory; CourseSearchIndex indexToClose = index) {
        }
    }

    @Test
    void emptyIndexAndUnknownTermsReturnEmptyResults() throws IOException {
        assertEquals(new CourseSearchIndex.SearchResult(0, List.of()), index.search("java", null, 10));
        index.upsert(course(1, "Java basics", "Learning language", CourseLevel.BEGINNER));
        index.refresh();
        assertTrue(index.search("python", null, 10).hits().isEmpty());
    }

    @Test
    void analyzerNormalizesEnglishAndRequiresAllTermsAcrossContent() throws IOException {
        index.upsert(course(1, "Java Basics", "Learn Redis caching", CourseLevel.BEGINNER));
        index.upsert(course(2, "Java concurrency", "Threads and locks", CourseLevel.ADVANCED));
        index.refresh();
        var result = index.search("JAVA REDIS", null, 10);
        assertEquals(1, result.totalHits());
        assertEquals(1, result.hits().getFirst().course().id());
        assertTrue(result.hits().getFirst().score() > 0);
        assertEquals(2, index.search("java", null, 10).totalHits());
    }

    @Test
    void cjkAnalysisMatchesChineseTermsAlongsideEnglish() throws IOException {
        index.upsert(course(1, "Java 缓存基础", "使用 Redis 共享缓存", CourseLevel.BEGINNER));
        index.upsert(course(2, "Java 并发基础", "线程与锁", CourseLevel.INTERMEDIATE));
        index.refresh();
        assertEquals(Set.of(1L), ids(index.search("缓存", null, 10)));
        assertEquals(Set.of(1L), ids(index.search("Redis 缓存", null, 10)));
        assertEquals(Set.of(2L), ids(index.search("并发", null, 10)));
    }

    @Test
    void exactLevelFilterRestrictsMatchesAndPreservesStoredFields() throws IOException {
        CourseDocument beginner = course(1, " Java Basics ", " Learn classes ", CourseLevel.BEGINNER);
        index.upsert(beginner);
        index.upsert(course(2, "Java Advanced", "Performance", CourseLevel.ADVANCED));
        index.refresh();
        var result = index.search("java", CourseLevel.BEGINNER, 10);
        assertEquals(1, result.totalHits());
        assertEquals(beginner, result.hits().getFirst().course());
        assertEquals(0, index.search("java", CourseLevel.INTERMEDIATE, 10).totalHits());
    }

    @Test
    void querySyntaxIsPlainTextAndTokenlessInputDoesNotBecomeMatchAll() throws IOException {
        index.upsert(course(1, "Java basics", "Learn classes", CourseLevel.BEGINNER));
        index.refresh();
        assertEquals(0, index.search("*:*", null, 10).totalHits());
        assertEquals(0, index.search("the and", null, 10).totalHits());
        assertEquals(0, index.search("level:BEGINNER", null, 10).totalHits());
        assertEquals(0, index.search("title:java", null, 10).totalHits());
    }

    @Test
    void upsertReplacesSameIdWithoutLeavingOldSearchTerms() throws IOException {
        index.upsert(course(1, "Java basics", "Classes", CourseLevel.BEGINNER));
        index.refresh();
        index.upsert(course(1, "Redis cache", "Shared storage", CourseLevel.INTERMEDIATE));
        assertEquals(1, index.search("java", null, 10).totalHits());
        index.refresh();
        assertEquals(0, index.search("java", null, 10).totalHits());
        assertEquals(1, index.search("redis", null, 10).totalHits());
        assertEquals(CourseLevel.INTERMEDIATE, index.search("redis", null, 10).hits().getFirst().course().level());
    }

    @Test
    void deletionOnlyBecomesVisibleAfterRefresh() throws IOException {
        index.upsert(course(1, "Java basics", "Classes", CourseLevel.BEGINNER));
        index.refresh();
        index.delete(1);
        index.delete(999);
        assertEquals(1, index.search("java", null, 10).totalHits());
        index.refresh();
        assertEquals(0, index.search("java", null, 10).totalHits());
    }

    @Test
    void commitPersistsButDoesNotRefreshExistingSearchSnapshot() throws IOException {
        index.upsert(course(1, "Java basics", "Classes", CourseLevel.BEGINNER));
        index.commit();
        assertEquals(0, index.search("java", null, 10).totalHits());
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            assertEquals(1, new IndexSearcher(reader).count(new TermQuery(new Term("content", "java"))));
        }
        index.refresh();
        assertEquals(1, index.search("java", null, 10).totalHits());
    }

    @Test
    void committedFileIndexSurvivesCloseAndReopen(@TempDir Path path) throws IOException {
        CourseDocument course = course(1, "Java 缓存", "Redis basics", CourseLevel.BEGINNER);
        try (Directory files = FSDirectory.open(path); CourseSearchIndex original = new CourseSearchIndex(files)) {
            original.upsert(course);
            original.commit();
        }
        try (Directory files = FSDirectory.open(path); CourseSearchIndex restored = new CourseSearchIndex(files)) {
            assertEquals(course, restored.search("缓存", null, 10).hits().getFirst().course());
        }
    }

    @Test
    void refreshWithoutCommitDoesNotPersistChangesOnClose(@TempDir Path path) throws IOException {
        try (Directory files = FSDirectory.open(path); CourseSearchIndex original = new CourseSearchIndex(files)) {
            original.upsert(course(1, "Java saved", "Committed", CourseLevel.BEGINNER));
            original.commit();
            original.upsert(course(2, "Java temporary", "Uncommitted", CourseLevel.BEGINNER));
            original.delete(1);
            original.refresh();
            assertEquals(Set.of(2L), ids(original.search("java", null, 10)));
        }
        try (Directory files = FSDirectory.open(path); CourseSearchIndex restored = new CourseSearchIndex(files)) {
            assertEquals(Set.of(1L), ids(restored.search("java", null, 10)));
        }
    }

    @Test
    void boundedTopHitsStillReportExactTotalBeyondDefaultThreshold() throws IOException {
        for (int id = 1; id <= 1100; id++) {
            index.upsert(course(id, "Java course " + id, "Learning classes", CourseLevel.BEGINNER));
        }
        index.refresh();
        var result = index.search("java", null, 3);
        assertEquals(1100, result.totalHits());
        assertEquals(3, result.hits().size());
        assertThrows(UnsupportedOperationException.class, () -> result.hits().clear());
    }

    @Test
    void invalidInputsFailWithoutMutatingIndex() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> course(0, "Java", "Classes", CourseLevel.BEGINNER));
        assertThrows(IllegalArgumentException.class, () -> course(1, " ", "Classes", CourseLevel.BEGINNER));
        assertThrows(IllegalArgumentException.class, () -> course(1, "x".repeat(201), "Classes", CourseLevel.BEGINNER));
        assertThrows(IllegalArgumentException.class, () -> course(1, "Java", " ", CourseLevel.BEGINNER));
        assertThrows(IllegalArgumentException.class, () -> course(1, "Java", "x".repeat(4001), CourseLevel.BEGINNER));
        assertThrows(NullPointerException.class, () -> course(1, "Java", "Classes", null));
        assertThrows(NullPointerException.class, () -> index.upsert(null));
        assertThrows(IllegalArgumentException.class, () -> index.delete(0));
        assertThrows(IllegalArgumentException.class, () -> index.search(null, null, 10));
        assertThrows(IllegalArgumentException.class, () -> index.search(" ", null, 10));
        assertThrows(IllegalArgumentException.class, () -> index.search("x".repeat(201), null, 10));
        assertThrows(IllegalArgumentException.class, () -> index.search("java", null, 0));
        assertThrows(IllegalArgumentException.class, () -> index.search("java", null, 51));
        index.refresh();
        assertEquals(0, index.search("java", null, 10).totalHits());
    }

    @Test
    void closeIsIdempotentAndDoesNotOwnCallerDirectory() throws IOException {
        index.close();
        index.close();
        assertThrows(IllegalStateException.class, () -> index.search("java", null, 10));
        assertThrows(IllegalStateException.class, () -> index.upsert(course(1, "Java", "Classes", CourseLevel.BEGINNER)));
        assertThrows(IllegalStateException.class, () -> index.delete(1));
        assertThrows(IllegalStateException.class, () -> index.refresh());
        assertThrows(IllegalStateException.class, () -> index.commit());
        assertDoesNotThrow(directory::listAll);
    }

    @Test
    void secondWriterIsRejectedWithoutClosingCallerDirectoryOrFirstWriter() throws IOException {
        assertThrows(LockObtainFailedException.class, () -> new CourseSearchIndex(directory));
        index.upsert(course(1, "Java", "Classes", CourseLevel.BEGINNER));
        index.refresh();
        assertEquals(1, index.search("java", null, 10).totalHits());
        directory.listAll();
    }

    private CourseDocument course(long id, String title, String description, CourseLevel level) {
        return new CourseDocument(id, title, description, level);
    }

    private Set<Long> ids(CourseSearchIndex.SearchResult result) {
        return result.hits().stream().map(hit -> hit.course().id()).collect(Collectors.toSet());
    }
}

package io.github.javamastery.exercises.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.QueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CourseSearchIndex implements AutoCloseable {
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final SearcherManager searchers;
    private boolean closed;

    public CourseSearchIndex(Directory directory) throws IOException {
        Objects.requireNonNull(directory);
        analyzer = new CJKAnalyzer();
        IndexWriter openedWriter = null;
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND).setCommitOnClose(false);
            openedWriter = new IndexWriter(directory, config);
            searchers = new SearcherManager(openedWriter, null);
            writer = openedWriter;
        } catch (IOException | RuntimeException | Error failure) {
            try {
                if (openedWriter != null) {
                    openedWriter.close();
                }
            } catch (IOException | RuntimeException cleanup) {
                failure.addSuppressed(cleanup);
            }
            try {
                analyzer.close();
            } catch (RuntimeException cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    public synchronized void upsert(CourseDocument course) throws IOException {
        requireOpen();
        Objects.requireNonNull(course);
        Document document = new Document();
        document.add(new StringField("id", Long.toString(course.id()), Field.Store.YES));
        document.add(new StoredField("title", course.title()));
        document.add(new StoredField("description", course.description()));
        document.add(new StringField("level", course.level().name(), Field.Store.YES));
        document.add(new TextField("content", course.title() + "\n" + course.description(), Field.Store.NO));
        writer.updateDocument(new Term("id", Long.toString(course.id())), document);
    }

    public synchronized void delete(long id) throws IOException {
        requireOpen();
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须为正数");
        }
        writer.deleteDocuments(new Term("id", Long.toString(id)));
    }

    public synchronized void refresh() throws IOException {
        requireOpen();
        searchers.maybeRefreshBlocking();
    }

    public synchronized void commit() throws IOException {
        requireOpen();
        writer.commit();
    }

    public synchronized SearchResult search(String text, CourseLevel level, int limit) throws IOException {
        requireOpen();
        if (text == null || text.isBlank() || text.length() > 200 || limit < 1 || limit > 50) {
            throw new IllegalArgumentException("关键词长度为 1 到 200 字符，结果上限为 1 到 50");
        }
        Query textQuery = new QueryBuilder(analyzer).createBooleanQuery("content", text, BooleanClause.Occur.MUST);
        if (textQuery == null) {
            return new SearchResult(0, List.of());
        }
        BooleanQuery.Builder query = new BooleanQuery.Builder().add(textQuery, BooleanClause.Occur.MUST);
        if (level != null) {
            query.add(new TermQuery(new Term("level", level.name())), BooleanClause.Occur.FILTER);
        }
        IndexSearcher searcher = searchers.acquire();
        try {
            Query combined = query.build();
            // Count on the same snapshot: TopDocs.totalHits can otherwise be only a lower bound.
            int totalHits = searcher.count(combined);
            var top = searcher.search(combined, limit);
            var stored = searcher.storedFields();
            List<SearchHit> hits = new ArrayList<>();
            for (var hit : top.scoreDocs) {
                Document document = stored.document(hit.doc);
                CourseDocument course = new CourseDocument(Long.parseLong(document.get("id")),
                        document.get("title"), document.get("description"), CourseLevel.valueOf(document.get("level")));
                hits.add(new SearchHit(course, hit.score));
            }
            return new SearchResult(totalHits, hits);
        } finally {
            searchers.release(searcher);
        }
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("搜索索引已关闭");
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            // Reverse close order releases readers, then writer, then analyzer; Directory belongs to the caller.
            try (Analyzer analyzerToClose = analyzer; IndexWriter writerToClose = writer;
                 SearcherManager searchersToClose = searchers) {
            }
        }
    }

    public record SearchHit(CourseDocument course, float score) {
    }

    public record SearchResult(int totalHits, List<SearchHit> hits) {
        public SearchResult {
            hits = List.copyOf(hits);
        }
    }
}

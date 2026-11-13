package io.github.javamastery.exercises.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;

import java.io.IOException;
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
        throw new UnsupportedOperationException("练习：按课程 ID 更新索引，区分存储字段与全文字段");
    }

    public synchronized void delete(long id) throws IOException {
        throw new UnsupportedOperationException("练习：校验课程 ID 并删除对应文档");
    }

    public synchronized void refresh() throws IOException {
        throw new UnsupportedOperationException("练习：刷新近实时查询快照");
    }

    public synchronized void commit() throws IOException {
        throw new UnsupportedOperationException("练习：显式提交索引，但不隐式刷新读快照");
    }

    public synchronized SearchResult search(String text, CourseLevel level, int limit) throws IOException {
        throw new UnsupportedOperationException("练习：使用一致分析器构建 AND 查询，过滤级别并返回有界结果与精确总数");
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

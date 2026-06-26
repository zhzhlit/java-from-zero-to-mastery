package io.github.javamastery.core.oop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningResourceTest {

    @Test
    void videoResourceReusesBaseStateAndOverridesBehavior() {
        VideoResource video = new VideoResource("  继承入门  ", 18, true);

        assertEquals("继承入门", video.getTitle());
        assertEquals(18, video.getDurationMinutes());
        assertEquals(18, video.estimatedMinutes());
        assertEquals("Video: 继承入门 - 18 min, captions available", video.summary());
    }

    @Test
    void articleResourceCalculatesItsOwnEstimatedMinutes() {
        ArticleResource article = new ArticleResource("多态阅读", 760);

        assertEquals("多态阅读", article.getTitle());
        assertEquals(760, article.getWordCount());
        assertEquals(4, article.estimatedMinutes());
        assertEquals("Article: 多态阅读 - 760 words, about 4 min", article.summary());
    }

    @Test
    void polymorphicReferencesCallSubclassImplementations() {
        LearningResource[] resources = {
                new VideoResource("继承视频", 15, false),
                new ArticleResource("多态文章", 500)
        };

        int totalMinutes = 0;
        StringBuilder summaries = new StringBuilder();
        for (LearningResource resource : resources) {
            totalMinutes += resource.estimatedMinutes();
            if (!summaries.isEmpty()) {
                summaries.append(System.lineSeparator());
            }
            summaries.append(resource.summary());
        }

        assertEquals(17, totalMinutes);
        assertEquals(String.join(System.lineSeparator(),
                "Video: 继承视频 - 15 min, no captions",
                "Article: 多态文章 - 500 words, about 2 min"), summaries.toString());
    }

    @Test
    void rejectsInvalidResourceState() {
        assertThrows(IllegalArgumentException.class,
                () -> new VideoResource(" ", 10, true));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoResource("视频", 0, true));
        assertThrows(IllegalArgumentException.class,
                () -> new ArticleResource("文章", -1));
    }
}

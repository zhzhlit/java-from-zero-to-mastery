package io.github.javamastery.exercises.oop.course;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningResourceTest {

    @Test
    void videoResourceExtendsBaseResource() {
        VideoResource video = new VideoResource("  继承视频  ", 25, false);

        assertEquals("继承视频", video.getTitle());
        assertEquals(25, video.getDurationMinutes());
        assertEquals(25, video.estimatedMinutes());
        assertEquals("Video: 继承视频 - 25 min, no captions", video.summary());
    }

    @Test
    void articleResourceProvidesDifferentBehavior() {
        ArticleResource article = new ArticleResource("多态文章", 640);

        assertEquals("多态文章", article.getTitle());
        assertEquals(640, article.getWordCount());
        assertEquals(3, article.estimatedMinutes());
        assertEquals("Article: 多态文章 - 640 words, about 3 min", article.summary());
    }

    @Test
    void parentReferencesDispatchToSubclassMethods() {
        LearningResource[] resources = {
                new VideoResource("视频", 12, true),
                new ArticleResource("文章", 250)
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

        assertEquals(13, totalMinutes);
        assertEquals(String.join(System.lineSeparator(),
                "Video: 视频 - 12 min, captions available",
                "Article: 文章 - 250 words, about 1 min"), summaries.toString());
    }

    @Test
    void rejectsInvalidResourceState() {
        assertThrows(IllegalArgumentException.class,
                () -> new VideoResource("", 10, true));
        assertThrows(IllegalArgumentException.class,
                () -> new VideoResource("视频", -1, true));
        assertThrows(IllegalArgumentException.class,
                () -> new ArticleResource("文章", 0));
    }
}

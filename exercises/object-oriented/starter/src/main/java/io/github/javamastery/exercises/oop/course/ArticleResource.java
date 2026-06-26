package io.github.javamastery.exercises.oop.course;

public class ArticleResource extends LearningResource {

    private final int wordCount;

    public ArticleResource(String title, int wordCount) {
        super(title);
        this.wordCount = wordCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    @Override
    public int estimatedMinutes() {
        return 0;
    }

    @Override
    protected String typeLabel() {
        return "";
    }

    @Override
    protected String detailText() {
        return "";
    }
}

package io.github.javamastery.core.oop;

public class ArticleResource extends LearningResource {

    private final int wordCount;

    public ArticleResource(String title, int wordCount) {
        super(title);
        if (wordCount <= 0) {
            throw new IllegalArgumentException("word count must be positive");
        }
        this.wordCount = wordCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    @Override
    public int estimatedMinutes() {
        return Math.max(1, (int) Math.ceil(wordCount / 250.0));
    }

    @Override
    protected String typeLabel() {
        return "Article";
    }

    @Override
    protected String detailText() {
        return wordCount + " words, about " + estimatedMinutes() + " min";
    }
}

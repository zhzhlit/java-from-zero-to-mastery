package io.github.javamastery.basics.strings;

public final class StringInspector {

    private StringInspector() {
    }

    public static String normalizeWhitespace(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        String stripped = text.strip();
        StringBuilder normalized = new StringBuilder();
        boolean previousWasWhitespace = false;
        for (int index = 0; index < stripped.length(); index++) {
            char current = stripped.charAt(index);
            if (Character.isWhitespace(current)) {
                if (!previousWasWhitespace) {
                    normalized.append(' ');
                }
                previousWasWhitespace = true;
            } else {
                normalized.append(current);
                previousWasWhitespace = false;
            }
        }
        return normalized.toString();
    }

    public static boolean sameText(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }
}

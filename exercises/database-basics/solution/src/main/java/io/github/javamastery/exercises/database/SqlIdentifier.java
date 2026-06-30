package io.github.javamastery.exercises.database;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class SqlIdentifier {
    private static final Pattern SIMPLE_IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Set<String> RESERVED_WORDS = Set.of(
            "select", "insert", "update", "delete", "from", "where", "table", "drop"
    );

    public String validate(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("SQL identifier must not be blank");
        }
        String trimmed = identifier.trim();
        if (!SIMPLE_IDENTIFIER.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
        if (RESERVED_WORDS.contains(trimmed.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Reserved SQL word: " + identifier);
        }
        return trimmed;
    }

    public String qualify(String table, String column) {
        return validate(table) + "." + validate(column);
    }
}

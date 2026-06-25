package io.github.javamastery.basics.strings;

public final class ReportBuilder {

    private ReportBuilder() {
    }

    public static String numberedLines(String[] items) {
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        StringBuilder report = new StringBuilder();
        for (int index = 0; index < items.length; index++) {
            if (index > 0) {
                report.append('\n');
            }
            report.append(index + 1).append(". ").append(items[index]);
        }
        return report.toString();
    }
}

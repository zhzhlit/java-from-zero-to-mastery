package io.github.javamastery.basics.debugging;

public final class DebuggingScenarios {

    private DebuggingScenarios() {
    }

    public static int findFirstIndex(int[] values, int target) {
        for (int index = 0; index < values.length; index++) {
            if (values[index] == target) {
                return index;
            }
        }
        return -1;
    }

    public static boolean containsName(String[] names, String target) {
        for (String name : names) {
            if (name.equals(target)) {
                return true;
            }
        }
        return false;
    }
}

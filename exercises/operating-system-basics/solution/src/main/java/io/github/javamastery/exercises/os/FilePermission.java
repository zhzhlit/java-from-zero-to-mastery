package io.github.javamastery.exercises.os;

public class FilePermission {
    private final String symbolic;

    private FilePermission(String symbolic) {
        this.symbolic = symbolic;
    }

    public static FilePermission parse(String symbolic) {
        if (symbolic == null || symbolic.length() != 9) {
            throw new IllegalArgumentException("permission must contain 9 characters");
        }
        validate(symbolic, 0, 'r');
        validate(symbolic, 1, 'w');
        validate(symbolic, 2, 'x');
        validate(symbolic, 3, 'r');
        validate(symbolic, 4, 'w');
        validate(symbolic, 5, 'x');
        validate(symbolic, 6, 'r');
        validate(symbolic, 7, 'w');
        validate(symbolic, 8, 'x');
        return new FilePermission(symbolic);
    }

    public boolean canOwnerRead() {
        return symbolic.charAt(0) == 'r';
    }

    public boolean canOwnerWrite() {
        return symbolic.charAt(1) == 'w';
    }

    public boolean canOwnerExecute() {
        return symbolic.charAt(2) == 'x';
    }

    public boolean canGroupRead() {
        return symbolic.charAt(3) == 'r';
    }

    public boolean canGroupWrite() {
        return symbolic.charAt(4) == 'w';
    }

    public boolean canGroupExecute() {
        return symbolic.charAt(5) == 'x';
    }

    public boolean canOthersRead() {
        return symbolic.charAt(6) == 'r';
    }

    public boolean canOthersWrite() {
        return symbolic.charAt(7) == 'w';
    }

    public boolean canOthersExecute() {
        return symbolic.charAt(8) == 'x';
    }

    public boolean isExecutableByAnyone() {
        return canOwnerExecute() || canGroupExecute() || canOthersExecute();
    }

    public String toOctal() {
        return "" + digit(0) + digit(3) + digit(6);
    }

    private int digit(int offset) {
        int value = 0;
        if (symbolic.charAt(offset) != '-') {
            value += 4;
        }
        if (symbolic.charAt(offset + 1) != '-') {
            value += 2;
        }
        if (symbolic.charAt(offset + 2) != '-') {
            value += 1;
        }
        return value;
    }

    private static void validate(String symbolic, int index, char expected) {
        char actual = symbolic.charAt(index);
        if (actual != expected && actual != '-') {
            throw new IllegalArgumentException("invalid permission character: " + actual);
        }
    }
}

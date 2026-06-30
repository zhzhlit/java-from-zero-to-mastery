package io.github.javamastery.exercises.os;

public class FilePermission {
    public static FilePermission parse(String symbolic) {
        return new FilePermission();
    }

    public boolean canOwnerRead() {
        return false;
    }

    public boolean canOwnerWrite() {
        return false;
    }

    public boolean canOwnerExecute() {
        return false;
    }

    public boolean canGroupRead() {
        return false;
    }

    public boolean canGroupWrite() {
        return false;
    }

    public boolean canGroupExecute() {
        return false;
    }

    public boolean canOthersRead() {
        return false;
    }

    public boolean canOthersWrite() {
        return false;
    }

    public boolean canOthersExecute() {
        return false;
    }

    public boolean isExecutableByAnyone() {
        return false;
    }

    public String toOctal() {
        return "000";
    }
}

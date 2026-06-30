package io.github.javamastery.exercises.network;

public class HttpStatusClassifier {
    public HttpStatusFamily familyOf(int statusCode) {
        return HttpStatusFamily.UNKNOWN;
    }

    public boolean isSuccessful(int statusCode) {
        return false;
    }
}

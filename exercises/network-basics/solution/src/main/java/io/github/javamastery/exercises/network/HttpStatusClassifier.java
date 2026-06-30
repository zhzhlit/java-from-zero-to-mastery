package io.github.javamastery.exercises.network;

public class HttpStatusClassifier {
    public HttpStatusFamily familyOf(int statusCode) {
        if (statusCode >= 100 && statusCode <= 199) {
            return HttpStatusFamily.INFORMATIONAL;
        }
        if (statusCode >= 200 && statusCode <= 299) {
            return HttpStatusFamily.SUCCESS;
        }
        if (statusCode >= 300 && statusCode <= 399) {
            return HttpStatusFamily.REDIRECTION;
        }
        if (statusCode >= 400 && statusCode <= 499) {
            return HttpStatusFamily.CLIENT_ERROR;
        }
        if (statusCode >= 500 && statusCode <= 599) {
            return HttpStatusFamily.SERVER_ERROR;
        }
        return HttpStatusFamily.UNKNOWN;
    }

    public boolean isSuccessful(int statusCode) {
        return familyOf(statusCode) == HttpStatusFamily.SUCCESS;
    }
}

package io.github.javamastery.exercises.http;

import java.util.function.Function;

public class SimpleRouter {
    public SimpleRouter get(String path, Function<HttpRequest, HttpResponse> handler) {
        return this;
    }

    public SimpleRouter post(String path, Function<HttpRequest, HttpResponse> handler) {
        return this;
    }

    public HttpResponse handle(HttpRequest request) {
        return HttpResponse.notFound("");
    }
}

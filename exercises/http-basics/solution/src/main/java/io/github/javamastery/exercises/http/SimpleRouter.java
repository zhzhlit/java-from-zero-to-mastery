package io.github.javamastery.exercises.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class SimpleRouter {
    private final Map<RouteKey, Function<HttpRequest, HttpResponse>> routes = new LinkedHashMap<>();

    public SimpleRouter get(String path, Function<HttpRequest, HttpResponse> handler) {
        routes.put(new RouteKey("GET", path), handler);
        return this;
    }

    public SimpleRouter post(String path, Function<HttpRequest, HttpResponse> handler) {
        routes.put(new RouteKey("POST", path), handler);
        return this;
    }

    public HttpResponse handle(HttpRequest request) {
        Function<HttpRequest, HttpResponse> handler = routes.get(new RouteKey(request.method(), request.path()));
        if (handler == null) {
            return HttpResponse.notFound("No route for " + request.method() + " " + request.path());
        }
        return handler.apply(request);
    }
}

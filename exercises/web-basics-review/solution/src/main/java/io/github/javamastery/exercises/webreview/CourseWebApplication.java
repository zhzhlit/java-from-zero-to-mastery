package io.github.javamastery.exercises.webreview;

import java.util.Set;

public class CourseWebApplication {
    private final CourseController controller;
    private final AuthFilter authFilter;

    public CourseWebApplication(CourseStore store, String writeToken) {
        this.controller = new CourseController(store);
        this.authFilter = new AuthFilter(Set.of("POST"), writeToken);
    }

    public ApiResponse handle(ApiRequest request) {
        ApiResponse blocked = authFilter.filter(request);
        if (blocked != null) {
            return blocked;
        }
        String method = request.method().toUpperCase();
        if ("GET".equals(method) && "/courses".equals(request.path())) {
            return controller.listPublished(request);
        }
        if ("GET".equals(method) && "/courses/detail".equals(request.path())) {
            return controller.show(request);
        }
        if ("POST".equals(method) && "/courses".equals(request.path())) {
            return controller.create(request);
        }
        if (("PUT".equals(method) || "DELETE".equals(method)) && request.path().startsWith("/courses")) {
            return new ApiResponse().status(405).header("Allow", "GET, POST").body("method not allowed");
        }
        return new ApiResponse().status(404).body("route not found");
    }
}

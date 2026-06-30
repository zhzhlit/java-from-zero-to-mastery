package io.github.javamastery.exercises.webreview;

public class CourseWebApplication {
    public CourseWebApplication(CourseStore store, String writeToken) {
    }

    public ApiResponse handle(ApiRequest request) {
        return new ApiResponse().status(404).body("route not found");
    }
}

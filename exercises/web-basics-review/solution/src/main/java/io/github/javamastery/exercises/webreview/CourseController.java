package io.github.javamastery.exercises.webreview;

import java.util.stream.Collectors;

public class CourseController {
    private final CourseStore store;

    public CourseController(CourseStore store) {
        this.store = store;
    }

    public ApiResponse listPublished(ApiRequest request) {
        String body = store.publishedCourses().stream()
                .map(course -> course.id() + ":" + course.title())
                .collect(Collectors.joining("\n"));
        return json(200, body);
    }

    public ApiResponse show(ApiRequest request) {
        long id = parseId(request.formValue("id"));
        return store.findById(id)
                .map(course -> json(200, course.id() + ":" + course.title() + ":" + course.lessonCount()))
                .orElseGet(() -> json(404, "course not found"));
    }

    public ApiResponse create(ApiRequest request) {
        String title = request.formValue("title").trim();
        if (title.isBlank()) {
            return json(400, "title is required");
        }
        int lessonCount = parseLessonCount(request.formValue("lessonCount"));
        CourseSummary course = store.create(title, lessonCount);
        return json(201, course.id() + ":" + course.title()).header("Location", "/courses/" + course.id());
    }

    private ApiResponse json(int status, String body) {
        return new ApiResponse().status(status).header("Content-Type", "text/plain; charset=utf-8").body(body);
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private int parseLessonCount(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}

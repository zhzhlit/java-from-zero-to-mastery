package io.github.javamastery.exercises.springbootjdbc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<CourseDto> list() {
        return service.listCourses();
    }

    @PostMapping("/{courseId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto enroll(@PathVariable("courseId") long courseId, @RequestBody EnrollRequest request) {
        return service.enroll(courseId, request.student());
    }

    public record EnrollRequest(String student) {
    }
}

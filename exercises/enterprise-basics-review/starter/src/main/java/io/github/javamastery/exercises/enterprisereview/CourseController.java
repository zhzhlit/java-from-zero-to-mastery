package io.github.javamastery.exercises.enterprisereview;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.security.Principal;

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
    public EnrollmentDto enroll(@PathVariable("courseId") long courseId, Principal principal) {
        return service.enroll(courseId, principal.getName());
    }
}

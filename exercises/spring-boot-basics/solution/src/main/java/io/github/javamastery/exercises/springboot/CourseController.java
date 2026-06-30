package io.github.javamastery.exercises.springboot;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseDto> listPublished() {
        return courseService.publishedCourses();
    }

    @GetMapping("/{id}")
    public CourseDto show(@PathVariable("id") long id) {
        return courseService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "course not found"));
    }

    @PostMapping
    public ResponseEntity<CourseDto> create(@RequestBody CreateCourseRequest request) {
        CourseDto course = courseService.create(request);
        return ResponseEntity.created(URI.create("/api/courses/" + course.id())).body(course);
    }
}

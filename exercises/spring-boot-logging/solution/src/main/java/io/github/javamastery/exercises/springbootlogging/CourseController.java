package io.github.javamastery.exercises.springbootlogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class CourseController {
    private static final Logger log = LoggerFactory.getLogger(CourseController.class);
    private final List<CourseDto> courses = List.of(new CourseDto(1, "Spring Boot Logging"));

    @GetMapping("/courses")
    public List<CourseDto> list() {
        log.info("event=course_list count={}", courses.size());
        log.debug("event=course_list_details source=sample");
        return courses;
    }

    @GetMapping("/courses/{id}")
    public CourseDto find(@PathVariable("id") long id) {
        return courses.stream().filter(course -> course.id() == id).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public record CourseDto(long id, String title) {
    }
}

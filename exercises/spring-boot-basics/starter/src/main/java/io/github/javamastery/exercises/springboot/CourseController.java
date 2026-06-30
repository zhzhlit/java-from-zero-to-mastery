package io.github.javamastery.exercises.springboot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    public CourseController(CourseService courseService) {
    }
}

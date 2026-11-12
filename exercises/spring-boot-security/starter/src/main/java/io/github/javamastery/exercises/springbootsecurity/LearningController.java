package io.github.javamastery.exercises.springbootsecurity;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LearningController {
    private final Map<String, Integer> progress = new ConcurrentHashMap<>();

    @GetMapping("/courses")
    public List<CourseDto> courses() {
        return List.of(new CourseDto(1, "Spring Security Basics"));
    }

    @GetMapping("/profile")
    public ProfileDto profile(Principal principal) {
        return new ProfileDto(principal.getName());
    }

    @GetMapping("/admin/summary")
    public SummaryDto summary() {
        return new SummaryDto(courses().size());
    }

    @GetMapping("/csrf")
    public CsrfDto csrf(CsrfToken token) {
        return new CsrfDto(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @GetMapping("/progress")
    public ProgressDto progress(Principal principal) {
        return new ProgressDto(principal.getName(), progress.getOrDefault(principal.getName(), 0));
    }

    @PostMapping("/progress")
    public ProgressDto updateProgress(Principal principal, @RequestBody ProgressRequest request) {
        if (request.completedLessons() == null || request.completedLessons() < 0 || request.completedLessons() > 10000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        progress.put(principal.getName(), request.completedLessons());
        return progress(principal);
    }

    public record CourseDto(long id, String title) {
    }

    public record ProfileDto(String username) {
    }

    public record SummaryDto(int courseCount) {
    }

    public record CsrfDto(String headerName, String parameterName, String token) {
    }

    public record ProgressRequest(Integer completedLessons) {
    }

    public record ProgressDto(String username, int completedLessons) {
    }
}

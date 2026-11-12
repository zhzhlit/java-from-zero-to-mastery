package io.github.javamastery.exercises.enterprisereview;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class AccountController {
    private final CourseService service;

    public AccountController(CourseService service) {
        this.service = service;
    }

    @GetMapping("/profile")
    public ProfileDto profile(Principal principal) {
        return new ProfileDto(principal.getName());
    }

    @GetMapping("/csrf")
    public CsrfDto csrf(CsrfToken token) {
        return new CsrfDto(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @GetMapping("/enrollments")
    public List<EnrollmentDto> ownEnrollments(Principal principal) {
        return service.enrollmentsFor(principal.getName());
    }

    @GetMapping("/admin/enrollments")
    public List<EnrollmentDto> allEnrollments() {
        return service.allEnrollments();
    }

    public record ProfileDto(String username) {
    }

    public record CsrfDto(String headerName, String parameterName, String token) {
    }
}

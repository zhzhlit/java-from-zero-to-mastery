package io.github.javamastery.exercises.springbootlayers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(CourseNotFoundException exception) {
        return new ErrorResponse("COURSE_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleBusinessRule(BusinessRuleException exception) {
        return new ErrorResponse("BUSINESS_RULE_VIOLATION", exception.getMessage());
    }
}

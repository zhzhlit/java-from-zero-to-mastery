package io.github.javamastery.exercises.springbootvalidation;

import java.util.List;

public record ApiErrorResponse(String code, String message, List<FieldErrorView> errors) {
}

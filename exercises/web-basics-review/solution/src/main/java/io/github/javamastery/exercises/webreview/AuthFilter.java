package io.github.javamastery.exercises.webreview;

import java.util.Set;

public class AuthFilter {
    private final Set<String> protectedMethods;
    private final String expectedToken;

    public AuthFilter(Set<String> protectedMethods, String expectedToken) {
        this.protectedMethods = protectedMethods == null ? Set.of() : Set.copyOf(protectedMethods);
        this.expectedToken = expectedToken;
    }

    public ApiResponse filter(ApiRequest request) {
        if (!protectedMethods.contains(request.method().toUpperCase())) {
            return null;
        }
        if (("Bearer " + expectedToken).equals(request.header("Authorization"))) {
            return null;
        }
        return new ApiResponse().status(401).header("WWW-Authenticate", "Bearer").body("unauthorized");
    }
}

package io.github.javamastery.exercises.webreview;

import java.util.Map;

public record ApiRequest(String method, String path, Map<String, String> headers, Map<String, String> form) {
    public ApiRequest {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        form = form == null ? Map.of() : Map.copyOf(form);
    }

    public String header(String name) {
        return "";
    }

    public String formValue(String name) {
        return "";
    }
}

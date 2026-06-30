package io.github.javamastery.exercises.servlet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServletDispatcher {
    private final Map<String, MiniServlet> servlets = new LinkedHashMap<>();
    private final List<ServletFilter> filters = new ArrayList<>();

    public ServletDispatcher register(String path, MiniServlet servlet) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            throw new IllegalArgumentException("path must start with /");
        }
        servlets.put(path, servlet);
        return this;
    }

    public ServletDispatcher addFilter(ServletFilter filter) {
        filters.add(filter);
        return this;
    }

    public WebResponse dispatch(WebRequest request) {
        WebResponse response = new WebResponse();
        MiniServlet servlet = servlets.get(request.path());
        if (servlet == null) {
            return response.status(404).write("No servlet for " + request.path());
        }
        new FilterChain(filters, servlet).doFilter(request, response);
        return response;
    }
}

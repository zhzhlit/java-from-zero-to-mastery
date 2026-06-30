package io.github.javamastery.exercises.servlet;

import java.util.List;

public class FilterChain {
    private final List<ServletFilter> filters;
    private final MiniServlet servlet;
    private int index;

    public FilterChain(List<ServletFilter> filters, MiniServlet servlet) {
        this.filters = List.copyOf(filters == null ? List.of() : filters);
        this.servlet = servlet;
    }

    public void doFilter(WebRequest request, WebResponse response) {
        if (index < filters.size()) {
            ServletFilter filter = filters.get(index);
            index++;
            filter.doFilter(request, response, this);
            return;
        }
        servlet.service(request, response);
    }
}

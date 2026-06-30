package io.github.javamastery.exercises.servlet;

public class ServletDispatcher {
    public ServletDispatcher register(String path, MiniServlet servlet) {
        return this;
    }

    public ServletDispatcher addFilter(ServletFilter filter) {
        return this;
    }

    public WebResponse dispatch(WebRequest request) {
        return new WebResponse();
    }
}

package io.github.javamastery.exercises.servlet;

public abstract class MiniServlet {
    public void service(WebRequest request, WebResponse response) {
        switch (request.method().toUpperCase()) {
            case "GET" -> doGet(request, response);
            case "POST" -> doPost(request, response);
            default -> methodNotAllowed(response);
        }
    }

    protected void doGet(WebRequest request, WebResponse response) {
        methodNotAllowed(response);
    }

    protected void doPost(WebRequest request, WebResponse response) {
        methodNotAllowed(response);
    }

    private void methodNotAllowed(WebResponse response) {
        response.status(405).header("Allow", "GET, POST").write("Method Not Allowed");
    }
}

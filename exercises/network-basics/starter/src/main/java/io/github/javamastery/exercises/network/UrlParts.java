package io.github.javamastery.exercises.network;

public class UrlParts {
    private final String scheme;
    private final String host;
    private final int port;
    private final String path;
    private final String query;

    public UrlParts(String scheme, String host, int port, String path, String query) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
    }

    public String scheme() {
        return scheme;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String path() {
        return path;
    }

    public String query() {
        return query;
    }
}

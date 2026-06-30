package io.github.javamastery.exercises.network;

import java.net.URI;
import java.util.Locale;

public class UrlInspector {
    public UrlParts parse(String url) {
        URI uri = URI.create(url);
        String scheme = requirePart(uri.getScheme(), "scheme").toLowerCase(Locale.ROOT);
        String host = requirePart(uri.getHost(), "host").toLowerCase(Locale.ROOT);
        int port = uri.getPort() == -1 ? defaultPortFor(scheme) : uri.getPort();
        String path = uri.getRawPath();
        String query = uri.getRawQuery();

        return new UrlParts(
                scheme,
                host,
                port,
                path == null || path.isBlank() ? "/" : path,
                query == null ? "" : query
        );
    }

    public String pathWithQuery(String url) {
        UrlParts parts = parse(url);
        if (parts.query().isBlank()) {
            return parts.path();
        }
        return parts.path() + "?" + parts.query();
    }

    private int defaultPortFor(String scheme) {
        if ("http".equals(scheme)) {
            return 80;
        }
        if ("https".equals(scheme)) {
            return 443;
        }
        return -1;
    }

    private String requirePart(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("URL " + name + " must not be blank");
        }
        return value;
    }
}

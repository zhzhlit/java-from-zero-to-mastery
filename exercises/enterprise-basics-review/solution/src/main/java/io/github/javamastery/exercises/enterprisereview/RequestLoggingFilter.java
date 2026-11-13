package io.github.javamastery.exercises.enterprisereview;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String candidate = request.getHeader(HEADER);
        String requestId = candidate != null && SAFE_ID.matcher(candidate).matches()
                ? candidate : UUID.randomUUID().toString();
        response.setHeader(HEADER, requestId);
        MDC.put(MDC_KEY, requestId);
        long started = System.nanoTime();
        boolean completed = false;
        try {
            chain.doFilter(request, response);
            completed = true;
        } finally {
            try {
                // Escaping exceptions have not yet reached the container's error handling.
                int status = completed ? response.getStatus() : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                log.info("event=http_request method={} status={} durationMs={} completed={}",
                        request.getMethod(), status, durationMs, completed);
            } finally {
                MDC.remove(MDC_KEY);
            }
        }
    }
}

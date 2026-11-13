package io.github.javamastery.exercises.springbootlogging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestLoggingTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private RequestLoggingFilter filter;
    private final Logger logger = (Logger) LoggerFactory.getLogger("io.github.javamastery.exercises.springbootlogging");
    private CapturingAppender appender;

    @BeforeEach
    void captureLogs() {
        MDC.clear();
        appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void releaseLogs() {
        logger.detachAppender(appender);
        appender.stop();
        MDC.clear();
    }

    @Test
    void generatesIdAndCorrelatesBusinessAndRequestLogs() throws Exception {
        String requestId = mvc.perform(get("/courses")).andExpect(status().isOk())
                .andReturn().getResponse().getHeader(RequestLoggingFilter.HEADER);

        assertEquals(requestId, UUID.fromString(requestId).toString());
        assertEquals(2, appender.list.size());
        for (ILoggingEvent event : appender.list) {
            assertEquals(requestId, event.getMDCPropertyMap().get(RequestLoggingFilter.MDC_KEY));
        }
        assertTrue(requestLog().getFormattedMessage().matches(
                "event=http_request method=GET status=200 durationMs=\\d+ completed=true"));
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
    }

    @Test
    void acceptsSafeIdIncludingMaximumLength() throws Exception {
        String requestId = "a".repeat(62) + "_-";
        mvc.perform(get("/courses").header(RequestLoggingFilter.HEADER, requestId))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestLoggingFilter.HEADER, requestId));

        assertEquals(requestId, requestLog().getMDCPropertyMap().get(RequestLoggingFilter.MDC_KEY));
    }

    @Test
    void replacesUnsafeAndOversizedIds() throws Exception {
        for (String candidate : List.of("", "bad id", "bad\r\nforged", "中文", "a".repeat(65))) {
            String requestId = mvc.perform(get("/courses").header(RequestLoggingFilter.HEADER, candidate))
                    .andExpect(status().isOk()).andReturn().getResponse().getHeader(RequestLoggingFilter.HEADER);

            assertNotEquals(candidate, requestId);
            assertEquals(requestId, UUID.fromString(requestId).toString());
        }
        assertFalse(appender.list.toString().contains("forged"));
    }

    @Test
    void recordsHandledHttpErrors() throws Exception {
        mvc.perform(get("/courses/99").header(RequestLoggingFilter.HEADER, "missing-course"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(RequestLoggingFilter.HEADER, "missing-course"));

        assertTrue(requestLog().getFormattedMessage().contains("status=404"));
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
    }

    @Test
    void cleansContextAndRethrowsUnhandledFailure() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/failure");
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletException failure = new ServletException("private failure details");

        ServletException actual = assertThrows(ServletException.class,
                () -> filter.doFilter(request, response, (req, res) -> { throw failure; }));

        assertSame(failure, actual);
        assertTrue(requestLog().getFormattedMessage().contains("status=500"));
        assertTrue(requestLog().getFormattedMessage().contains("completed=false"));
        assertFalse(requestLog().getFormattedMessage().contains("private failure details"));
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
    }

    @Test
    void doesNotLeakIdToNextRequestOnSameThread() throws Exception {
        String first = mvc.perform(get("/courses")).andReturn().getResponse().getHeader(RequestLoggingFilter.HEADER);
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
        String second = mvc.perform(get("/courses")).andReturn().getResponse().getHeader(RequestLoggingFilter.HEADER);

        assertNotEquals(first, second);
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
    }

    @Test
    void excludesSensitiveInputFromApplicationLogs() throws Exception {
        mvc.perform(get("/courses").queryParam("token", "private-query")
                        .header("Authorization", "Bearer private-auth")
                        .header("Cookie", "session=private-cookie").content("private-body"))
                .andExpect(status().isOk());

        for (ILoggingEvent event : appender.list) {
            assertFalse(event.getFormattedMessage().contains("private-"));
            assertFalse(event.getMDCPropertyMap().toString().contains("private-"));
        }
    }

    @Test
    void preservesOtherMdcKeysAndUsesConfiguredLevel() throws Exception {
        MDC.put("otherContext", "keep-me");
        mvc.perform(get("/courses")).andExpect(status().isOk());

        assertEquals("keep-me", MDC.get("otherContext"));
        assertNull(MDC.get(RequestLoggingFilter.MDC_KEY));
        assertTrue(logger.isInfoEnabled());
        assertFalse(logger.isDebugEnabled());
        assertFalse(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("course_list_details")));
    }

    private ILoggingEvent requestLog() {
        return appender.list.stream().filter(event -> event.getLoggerName().equals(RequestLoggingFilter.class.getName()))
                .findFirst().orElseThrow();
    }

    private static class CapturingAppender extends ListAppender<ILoggingEvent> {
        @Override
        protected void append(ILoggingEvent event) {
            event.prepareForDeferredProcessing();
            super.append(event);
        }
    }
}

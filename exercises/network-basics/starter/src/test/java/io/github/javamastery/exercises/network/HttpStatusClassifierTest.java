package io.github.javamastery.exercises.network;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HttpStatusClassifier")
class HttpStatusClassifierTest {
    private final HttpStatusClassifier classifier = new HttpStatusClassifier();

    @Test
    @Disabled("练习：移除 @Disabled，识别 2xx 成功状态")
    @DisplayName("识别 2xx 成功状态")
    void recognizesSuccessStatus() {
        assertEquals(HttpStatusFamily.SUCCESS, classifier.familyOf(200));
        assertTrue(classifier.isSuccessful(204));
    }

    @Test
    @Disabled("练习：移除 @Disabled，识别常见状态码族")
    @DisplayName("识别重定向、客户端错误和服务端错误")
    void recognizesCommonStatusFamilies() {
        assertEquals(HttpStatusFamily.REDIRECTION, classifier.familyOf(301));
        assertEquals(HttpStatusFamily.CLIENT_ERROR, classifier.familyOf(404));
        assertEquals(HttpStatusFamily.SERVER_ERROR, classifier.familyOf(503));
        assertFalse(classifier.isSuccessful(500));
    }

    @Test
    @Disabled("练习：移除 @Disabled，处理范围外状态码")
    @DisplayName("范围外状态码归为 UNKNOWN")
    void treatsOutOfRangeStatusAsUnknown() {
        assertEquals(HttpStatusFamily.UNKNOWN, classifier.familyOf(99));
        assertEquals(HttpStatusFamily.UNKNOWN, classifier.familyOf(600));
    }
}

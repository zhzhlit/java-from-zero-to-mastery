package io.github.javamastery.exercises.network;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("QueryParameters")
class QueryParametersTest {
    private final QueryParameters queryParameters = new QueryParameters();

    @Test
    @Disabled("练习：移除 @Disabled，解析重复参数并保留顺序")
    @DisplayName("解析重复参数并保留顺序")
    void parsesRepeatedParametersInOrder() {
        Map<String, List<String>> parameters = queryParameters.parse("tag=java&tag=http&keyword=Java+21");

        assertIterableEquals(List.of("tag", "keyword"), parameters.keySet());
        assertEquals(List.of("java", "http"), parameters.get("tag"));
        assertEquals(List.of("Java 21"), parameters.get("keyword"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，处理空值参数")
    @DisplayName("处理问号、空值和无等号参数")
    void parsesQuestionMarkAndBlankValues() {
        Map<String, List<String>> parameters = queryParameters.parse("?empty=&flag");

        assertEquals(List.of(""), parameters.get("empty"));
        assertEquals(List.of(""), parameters.get("flag"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，空查询串返回空 Map")
    @DisplayName("空查询串返回空 Map")
    void returnsEmptyMapForBlankQuery() {
        assertTrue(queryParameters.parse("").isEmpty());
        assertTrue(queryParameters.parse(null).isEmpty());
    }

    @Test
    @Disabled("练习：移除 @Disabled，读取第一个参数值")
    @DisplayName("读取第一个参数值")
    void returnsFirstValue() {
        Map<String, List<String>> parameters = queryParameters.parse("topic=network&topic=http");

        assertEquals("network", queryParameters.firstValue(parameters, "topic"));
        assertEquals("", queryParameters.firstValue(parameters, "missing"));
    }
}

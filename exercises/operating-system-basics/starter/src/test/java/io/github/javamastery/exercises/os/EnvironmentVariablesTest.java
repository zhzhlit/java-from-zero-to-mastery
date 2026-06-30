package io.github.javamastery.exercises.os;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("EnvironmentVariables")
class EnvironmentVariablesTest {
    private final EnvironmentVariables variables = new EnvironmentVariables();

    @Test
    @Disabled("练习：移除 @Disabled，读取必需环境变量")
    @DisplayName("读取必需环境变量")
    void readsRequiredVariable() {
        assertEquals("prod", variables.required(Map.of("APP_ENV", "prod"), "APP_ENV"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，缺失或空值时抛出异常")
    @DisplayName("必需环境变量缺失或为空时抛出异常")
    void rejectsMissingRequiredVariable() {
        assertThrows(IllegalArgumentException.class, () -> variables.required(Map.of(), "APP_ENV"));
        assertThrows(IllegalArgumentException.class, () -> variables.required(Map.of("APP_ENV", " "), "APP_ENV"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，为缺失变量提供默认值")
    @DisplayName("可为缺失变量提供默认值")
    void returnsDefaultForMissingVariable() {
        assertEquals("dev", variables.withDefault(Map.of(), "APP_ENV", "dev"));
        assertEquals("prod", variables.withDefault(Map.of("APP_ENV", "prod"), "APP_ENV", "dev"));
    }

    @Test
    @Disabled("练习：移除 @Disabled，拆分 PATH 并跳过空片段")
    @DisplayName("按分隔符拆分 PATH 并跳过空片段")
    void splitsPathEntries() {
        Map<String, String> environment = Map.of("PATH", "/usr/bin:/opt/java::/bin");

        assertEquals(List.of("/usr/bin", "/opt/java", "/bin"), variables.pathEntries(environment, ":"));
    }
}

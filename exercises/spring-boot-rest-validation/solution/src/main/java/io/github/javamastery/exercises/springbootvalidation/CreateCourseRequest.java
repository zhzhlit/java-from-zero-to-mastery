package io.github.javamastery.exercises.springbootvalidation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank(message = "课程标题不能为空")
        @Size(max = 60, message = "课程标题不能超过 60 个字符")
        String title,

        @Min(value = 1, message = "课时数至少为 1")
        @Max(value = 200, message = "课时数不能超过 200")
        int lessonCount,

        @NotBlank(message = "课程级别不能为空")
        @Pattern(regexp = "beginner|intermediate|advanced", message = "课程级别必须是 beginner、intermediate 或 advanced")
        String level
) {
}

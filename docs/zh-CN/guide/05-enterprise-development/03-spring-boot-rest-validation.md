---
title: Spring Boot REST 校验
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-07-03"
---

# Spring Boot REST 校验

## 学习目标

- 理解为什么 REST API 需要在边界校验请求。
- 能使用 Jakarta Bean Validation 为请求 DTO 声明规则。
- 能在 Controller 中使用 `@Valid` 触发校验。
- 能用 `@RestControllerAdvice` 返回稳定的错误 JSON。
- 能用 `MockMvc` 覆盖成功、校验失败和 404 场景。

## 前置知识

已完成 [Spring Boot 配置](./02-spring-boot-configuration.md)，知道 Controller、Service、DTO 和测试的基本职责。本章继续处理企业应用中非常常见的输入质量问题。

## 练习入口

本章对应仓库中的 Spring Boot REST 校验练习：

- [`exercises/spring-boot-rest-validation/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-rest-validation/starter)：学习者编辑区。
- [`exercises/spring-boot-rest-validation/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-rest-validation/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/spring-boot-rest-validation/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/spring-boot-rest-validation/solution -am test
```

## 请求 DTO 校验

REST API 不能假设客户端永远传入正确数据。创建课程时，标题、课时数和级别都应该先在请求边界被校验：

```java
public record CreateCourseRequest(
        @NotBlank(message = "课程标题不能为空")
        @Size(max = 60, message = "课程标题不能超过 60 个字符")
        String title,

        @Min(value = 1, message = "课时数至少为 1")
        @Max(value = 200, message = "课时数不能超过 200")
        int lessonCount,

        @Pattern(regexp = "beginner|intermediate|advanced")
        String level
) {
}
```

这些规则表达的是“请求能不能进入业务流程”。真正的业务判断仍然应该放在 Service 或领域对象中。

## 在 Controller 启用校验

DTO 上有注解还不够，Controller 需要在请求体参数上标注 `@Valid`：

```java
@PostMapping
public ResponseEntity<CourseDto> create(@Valid @RequestBody CreateCourseRequest request) {
    CourseDto course = courseService.create(request);
    return ResponseEntity.created(URI.create("/api/courses/" + course.id())).body(course);
}
```

当请求不合法时，Spring MVC 会抛出 `MethodArgumentNotValidException`，请求不会进入 Service。

## 统一错误响应

生产 API 不应该把框架默认异常直接暴露给前端。可以使用 `@RestControllerAdvice` 把校验错误转换成稳定结构：

```java
public record ApiErrorResponse(String code, String message, List<FieldErrorView> errors) {
}
```

返回示例：

```json
{
  "code": "VALIDATION_FAILED",
  "message": "请求参数校验失败",
  "errors": [
    {"field": "lessonCount", "message": "课时数至少为 1"}
  ]
}
```

稳定错误结构便于前端展示字段提示，也便于自动化测试断言。

## MockMvc 测试

本章练习继续使用轻量 `MockMvc`：

```java
MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new CourseController(new CourseService()))
        .setControllerAdvice(new ApiExceptionHandler())
        .build();
```

测试重点包括：

- 合法请求返回 201 和 `Location`。
- 非法请求返回 400、错误码和字段错误。
- 查询不存在课程时返回 404。

## 分级练习

- **基础**：为 `CreateCourseRequest` 添加非空、长度、范围和枚举值校验。
- **进阶**：在 Controller 中启用 `@Valid`，让非法请求停在边界。
- **挑战**：补全 `ApiExceptionHandler`，返回按字段名排序的错误列表。

## 常见错误与排查

- 只添加校验注解，忘记在 Controller 参数上写 `@Valid`。
- 缺少 `spring-boot-starter-validation` 依赖，校验不会按预期工作。
- 错误响应结构不稳定，前端和测试都难以依赖。
- 在 Controller 中堆积业务判断，导致边界校验和业务规则混在一起。
- 测试中没有注册 `ControllerAdvice`，导致断言不到自定义错误 JSON。

## 面试与复习题

1. 请求 DTO 校验适合处理哪些问题？
2. `@Valid` 应该写在哪里？
3. 为什么校验失败通常返回 400？
4. `@RestControllerAdvice` 适合统一处理哪些异常？
5. 字段错误响应为什么要保持稳定结构？
6. 边界校验和业务规则有什么区别？

## 本章总结

Spring Boot REST 校验的重点是把坏请求挡在 Controller 边界，并把失败原因变成调用方能理解的稳定响应。完成本章后，你已经能为创建类 API 建立基本的输入防线。

## 下一步

上一章：[Spring Boot 配置](./02-spring-boot-configuration.md)

继续按照[学习路线](../../roadmap/index.md)进入分层架构与事务。

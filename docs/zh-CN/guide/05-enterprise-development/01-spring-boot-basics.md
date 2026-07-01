---
title: Spring Boot 基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# Spring Boot 基础

## 学习目标

- 理解 Spring Boot 在 Java Web 开发中的位置。
- 能说明 `@SpringBootApplication`、自动配置和组件扫描的基本作用。
- 能使用 `@RestController` 编写 JSON 接口。
- 能把 Controller、Service 和 DTO 分成清晰边界。
- 能使用 `MockMvc` 验证状态码、响应头和 JSON 字段。

## 前置知识

已完成 [Web 基础综合复盘](../04-database-web/05-web-basics-review.md)，知道一个后端请求会经过过滤器、分派、控制器和数据访问边界。Spring Boot 会把这些常见边界标准化，并提供自动配置。

## 练习入口

本章对应仓库中的 Spring Boot 基础练习：

- [`exercises/spring-boot-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-basics/starter)：学习者编辑区。
- [`exercises/spring-boot-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/spring-boot-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/spring-boot-basics/solution -am test
```

## 最小启动类

Spring Boot 应用通常从一个启动类开始：

```java
@SpringBootApplication
public class SpringBootBasicsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootBasicsApplication.class, args);
    }
}
```

`@SpringBootApplication` 组合了自动配置、组件扫描和配置类能力。它让 Spring Boot 能找到同包及子包下的 Controller、Service 等组件。

## Controller 与 JSON 接口

`@RestController` 表示这个类的方法直接写 HTTP 响应体。示例中的课程接口包含：

- `GET /api/courses`：返回已发布课程列表。
- `GET /api/courses/{id}`：返回课程详情，找不到时返回 404。
- `POST /api/courses`：创建草稿课程，成功时返回 201 和 `Location`。

```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @GetMapping
    public List<CourseDto> listPublished() {
        return courseService.publishedCourses();
    }
}
```

Spring MVC 会把返回对象序列化成 JSON。

## Service 边界

Controller 负责 HTTP 边界，Service 负责业务边界。本章的 `CourseService` 使用内存数据，后续可以替换成 JDBC、JPA 或其他持久化实现。

把逻辑放在 Service 中有两个好处：

- Controller 更薄，专注请求和响应。
- Service 可以用普通单元测试验证，不必每次启动 MVC 测试环境。

## DTO 边界

DTO 是接口输入输出对象：

```java
public record CourseDto(long id, String title, int lessonCount, boolean published) {
}
```

不要直接把数据库对象暴露给外部接口。DTO 能让接口契约更稳定，也方便隐藏内部字段。

## MVC 测试

本章练习使用 `MockMvcBuilders.standaloneSetup` 验证 Controller 的 HTTP 行为，不启动完整 Spring 上下文：

```java
mockMvc.perform(get("/api/courses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("JDBC 基础"));
```

测试重点包括：

- 状态码是否正确。
- JSON 字段是否符合契约。
- 创建接口是否返回 `Location`。
- 找不到资源时是否返回 404。

## 分级练习

- **基础**：完成 Service，能列出发布课程、查询详情和创建草稿。
- **进阶**：完成 Controller 的 `GET` 与 `POST` 接口。
- **挑战**：使用 `MockMvc` 验证 JSON、状态码和响应头。

## 常见错误与排查

- Controller 没有被扫描：确认启动类包名在共同上层包。
- 忘记 `@RequestBody`：POST JSON 无法绑定到请求对象。
- 创建成功仍返回 200：创建资源应优先使用 201。
- 测试启动过重：Controller 测试可以先使用轻量 `MockMvc`。
- Service 中返回内部可变集合：应返回不可随意修改的视图或副本。

## 面试与复习题

1. `@SpringBootApplication` 做了哪些事？
2. `@RestController` 和普通 `@Controller` 有什么区别？
3. Controller 与 Service 的职责边界是什么？
4. DTO 为什么不应该直接等同于数据库表对象？
5. `ResponseEntity.created` 适合什么场景？
6. `MockMvc` 适合验证 Controller 的哪些行为？

## 本章总结

Spring Boot 基础的重点是从“手写 Web 边界”走向“框架管理 Web 边界”。完成本章后，你已经能编写一个最小 REST API，并用 MVC 测试验证接口契约。

## 下一步

上一章：[Web 基础综合复盘](../04-database-web/05-web-basics-review.md)

下一章：[Spring Boot 配置](./02-spring-boot-configuration.md)

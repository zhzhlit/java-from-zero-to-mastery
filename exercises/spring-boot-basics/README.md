# Spring Boot 基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的 [Spring Boot 基础](../../docs/zh-CN/guide/05-enterprise-development/01-spring-boot-basics.md)，再进入 `starter`。本练习使用真实 Spring Boot Web 与 MockMvc，重点是掌握启动类、Controller、Service、DTO 和 JSON 接口测试。

验证 starter：

```bash
mvn -B -pl exercises/spring-boot-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/spring-boot-basics/solution -am test
```

## 练习

1. `SpringBootBasicsApplication`：理解 `@SpringBootApplication` 和启动入口。
2. `CourseDto` 与 `CreateCourseRequest`：定义接口输入输出对象。
3. `CourseService`：提供已发布课程列表、详情查询和草稿创建。
4. `CourseController`：实现 REST 接口和状态码。
5. `CourseControllerTest`：使用 `@WebMvcTest` 与 `MockMvc` 测试 MVC 边界。

## 完成标准

完成本练习后，你应该能做到：

- 说明 Spring Boot 自动配置和组件扫描的基本作用。
- 使用 `@RestController`、`@GetMapping`、`@PostMapping` 暴露接口。
- 使用 `ResponseEntity.created` 返回 `201` 与 `Location`。
- 使用 `MockMvc` 验证 JSON 响应和状态码。

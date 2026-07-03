# Spring Boot REST 校验练习

本模块练习 Spring Boot REST API 的请求校验、统一错误响应和轻量 MVC 测试。

## 练习目标

- 使用 Jakarta Bean Validation 描述请求 DTO 规则。
- 在 Controller 中启用 `@Valid`。
- 将校验失败转换为稳定的 400 JSON 响应。
- 使用 `MockMvc` 验证成功创建、校验失败和 404 场景。

## 模块结构

- `starter`：保留可编译骨架，测试默认 `@Disabled`。
- `solution`：参考实现，测试全部启用。

## 验证

```bash
mvn -B -pl exercises/spring-boot-rest-validation/starter -am test
mvn -B -pl exercises/spring-boot-rest-validation/solution -am test
```

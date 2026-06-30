---
title: Web 基础综合复盘
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# Web 基础综合复盘

## 学习目标

- 把数据库表、Repository、HTTP 请求响应和 Servlet 分派放进同一条调用链。
- 能设计一个小型课程 Web API 的核心边界。
- 能使用状态码表达成功、创建、未认证、未找到和方法不允许。
- 能解释控制器、过滤器和数据访问各自应该放什么逻辑。
- 能为后续 Spring Boot 分层架构建立直觉。

## 前置知识

已完成 [Servlet 基础](./04-servlet-basics.md)，并了解数据库基础、JDBC 基础和 HTTP 基础。本章不引入外部 Web 框架，而是用纯 Java 做一次综合复盘。

## 练习入口

本章对应仓库中的 Web 基础综合练习：

- [`exercises/web-basics-review/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/web-basics-review/starter)：学习者编辑区。
- [`exercises/web-basics-review/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/web-basics-review/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/web-basics-review/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/web-basics-review/solution -am test
```

## 一条完整调用链

一个后端接口通常会经过这些边界：

```text
HTTP 请求 -> 过滤器 -> Dispatcher -> Controller -> Repository -> 数据 -> Response
```

每一层只做自己的事：

- 过滤器处理认证、日志、跨域等横切逻辑。
- Dispatcher 根据方法和路径找到处理入口。
- Controller 读取请求、校验输入、组织响应。
- Repository 隐藏数据存储细节。
- Response 统一承载状态码、头部和正文。

## 状态码是接口契约

接口不应该只返回字符串。客户端需要状态码判断结果：

- `200`：读取成功。
- `201`：创建成功，通常配合 `Location` 响应头。
- `400`：请求参数不合法。
- `401`：缺少或无效认证。
- `404`：资源或路径不存在。
- `405`：路径存在，但 HTTP 方法不支持。

练习中的课程 API 会覆盖这些常见状态。

## 控制器边界

控制器适合处理请求和响应的业务编排：

```java
ApiResponse response = controller.create(request);
```

它可以读取表单参数、调用 Repository、选择状态码和响应正文。但它不应该直接承担认证过滤，也不应该把数据存储细节散落在方法里。

## 过滤器边界

写操作通常需要保护。练习中的 `AuthFilter` 会检查 `POST` 请求是否携带 Bearer token：

```text
Authorization: Bearer secret
```

认证失败时直接返回 `401`，请求不会进入控制器。这和真实 Web 应用里的认证过滤器思路一致。

## Repository 边界

本章使用 `InMemoryCourseStore` 模拟数据访问。真实项目中，这一层可能由 JDBC、JPA 或 MyBatis 实现。控制器只依赖 `CourseStore` 接口，因此不需要关心底层数据来自内存还是数据库。

## 分级练习

- **基础**：完成请求、响应和内存数据仓库。
- **进阶**：完成课程列表、详情和创建控制器。
- **挑战**：完成认证过滤器和应用分派入口，正确处理 401、404、405。

## 常见错误与排查

- 控制器直接保存全局数据：应通过 Repository 边界访问数据。
- 所有错误都返回 200：应使用状态码表达不同结果。
- 把认证写进每个控制器方法：适合放进过滤器。
- 创建成功没有 `Location`：客户端很难知道新资源地址。
- 404 和 405 混用：路径不存在是 404，路径存在但方法不支持是 405。

## 面试与复习题

1. 一个 Web API 请求通常经过哪些层？
2. 过滤器和控制器分别适合放什么逻辑？
3. 为什么创建成功常用 `201` 而不是 `200`？
4. 401、404、405 分别表示什么？
5. 为什么控制器依赖 Repository 接口会更容易测试？
6. 这一章的纯 Java 模型和后续 Spring Boot 有哪些对应关系？

## 本章总结

Web 基础综合复盘的重点是把零散知识连成可解释的后端调用链。完成本章后，你已经能用清晰边界描述一个小型 Web API，也为后续 Spring Boot、分层架构、校验、事务和安全课程做好准备。

## 下一步

上一章：[Servlet 基础](./04-servlet-basics.md)

下一章：[Spring Boot 基础](../05-enterprise-development/01-spring-boot-basics.md)

---
title: Spring Boot 日志与请求追踪
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# Spring Boot 日志与请求追踪

## 学习目标

- 使用 SLF4J 参数化日志，理解 INFO、WARN、ERROR、DEBUG 的用途。
- 为同步请求建立可检索的请求 ID，关联业务与完成日志。
- 正确清理 MDC，避免线程复用时串入其他请求的信息。
- 限制日志字段，避免泄露调用方的敏感输入。

## 前置知识

已完成 [Spring Boot JDBC 与事务](./05-spring-boot-jdbc-transactions.md)，熟悉 Controller 和异常传播。本章新增独立的小型课程查询模块，专注日志，不复制上一章的报名与数据库代码。技术基线继续使用 Java 21、Spring Boot 3.3.7。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-logging/starter)：过滤器骨架与禁用测试。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-logging/solution)：完整过滤器和 8 个测试。

```bash
mvn -B -pl exercises/spring-boot-logging/starter -am test
mvn -B -pl exercises/spring-boot-logging/solution -am test
```

starter 初始跳过 8 项；完成后移除测试类上的 `@Disabled`，预期全部通过。

## 日志门面与级别

沿用 Web starter 自带的 SLF4J 门面与 Logback 实现，不额外添加其他日志框架。课程列表使用占位符，不拼接输入：

```java
private static final Logger log = LoggerFactory.getLogger(CourseController.class);

log.info("event=course_list count={}", courses.size());
log.debug("event=course_list_details source=sample");
```

- INFO：常规业务事件和请求完成信息。
- WARN：值得关注但业务可能继续的异常状态。
- ERROR：需要定位的失败，不要对同一个异常在每层重复打印。
- DEBUG：临时诊断细节，默认不输出，开启后也不能泄露秘密。

`System.out.println` 不提供统一级别、日志上下文等能力。项目配置默认 INFO，业务包也设为 INFO；只对需要诊断的包短期开启 DEBUG，不把所有框架日志全量打开。配置规则见 [Spring Boot 3.3 日志文档](https://docs.spring.io/spring-boot/3.3/reference/features/logging.html)。

## 校验请求 ID

过滤器读取 `X-Request-Id`，只接受完整匹配 `[A-Za-z0-9_-]{1,64}` 的值。缺失、空串、空格、换行、非 ASCII 字符或超长值都替换为 UUID。

这样既便于关联，也防止任意输入进入日志前缀或响应头。不要把请求 ID 当成登录身份、授权依据或业务幂等键；客户端提供的值可能重复。本章不把非法原值写入日志。

生成或接受后的 ID 写入响应头，调用方可以用它反馈问题：

```bash
curl -i -H 'X-Request-Id: demo-001' http://localhost:8080/courses
```

预期响应为 `200`，响应头包含 `X-Request-Id: demo-001`。不传请求 ID 时，响应头包含一个 UUID。

## MDC 与线程复用

MDC 为当前线程关联键值，不需要给每个业务方法加一个日志参数。过滤器在进入处理链前设置 `requestId`，Controller 内的 SLF4J 日志也能读取它：

```java
MDC.put("requestId", requestId);
try {
    chain.doFilter(request, response);
} finally {
    MDC.remove("requestId");
}
```

这是清理流程摘录。完整过滤器在移除键之前还会记录完成信息。线程会被容器复用，因此正常与异常路径都必须清理；只移除当前组件拥有的键，不随意 `MDC.clear()` 删除其他组件的上下文。MDC 不自动跨线程传播，本章不覆盖异步请求和线程池，见 [Logback MDC 文档](https://logback.qos.ch/manual/mdc.html)。

配置把该键输出到日志级别旁边：

```properties
logging.level.root=INFO
logging.level.io.github.javamastery.exercises.springbootlogging=INFO
logging.pattern.level=%5p [requestId=%X{requestId:-none}]
```

没有请求上下文的启动日志显示 `requestId=none`，这是正常现象。

## 完成日志与异常边界

过滤器使用 `System.nanoTime()` 计算耗时，日志包含：

```text
event=http_request method=GET status=200 durationMs=3 completed=true
```

`completed=true` 表示处理链正常返回，不代表业务一定成功。比如 `/courses/99` 被 MVC 转换为 `404`，处理链仍然正常返回，完成日志记录 `status=404`。

如果异常直接逃出处理链，过滤器记录 `status=500 completed=false`，继续抛出原异常。此时容器还没完成错误处理，这个 500 是过滤器的失败分类，不是最终线上响应的保证。过滤器不会吞掉异常，也不把异常消息、SQL 或堆栈里的输入复制进请求完成日志。

记录耗时使用单调时钟，不用两次系统日期相减；不要在测试里断言固定毫秒数。这里的日志是可读 `key=value` 文本，不是 JSON 日志，也不是分布式追踪系统。

## 日志中的敏感信息

本模块的应用日志仅输出固定事件名、课程数量、HTTP 方法、状态、耗时和经过校验的请求 ID，不输出原始 URL、查询参数、正文、Cookie、Authorization 等字段。即使业务认为某字段“只是调试用”，也不能默认整包记录请求。

这不表示框架日志、代理访问日志、容器日志或未来新增日志都已自动脱敏。生产环境仍需单独审查日志配置、权限、保留周期和采集端处理。

## 分级练习

- **基础**：运行课程列表，分别以 INFO、DEBUG 启动，观察额外诊断事件。
- **进阶**：完成请求 ID 校验、响应头与 MDC 设置，让业务日志和完成日志共享 ID。
- **挑战**：完成正常、404、异常路径的日志与清理，验证同一线程处理下一次请求时不会沿用旧 ID。

完整启动命令见 [练习 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-logging#readme)。

## 验证与排查

测试捕获真实 Logback 事件，并在 MDC 清理前保存快照，检查业务与请求日志的 ID 一致。另有测试主动让过滤器链抛出异常，确认原异常继续传播且 MDC 已清理。

- 找不到 ID：检查过滤器是否注册、日志模板是否包含 `%X{requestId}`。
- ID 串请求：检查是否遗漏 `finally`，不要只在成功路径清理。
- DEBUG 没输出：检查业务包的显式级别，而不只看 root。
- 测试拿不到 MDC：测试 appender 必须在日志事件产生时固定 MDC 快照，不能在清理之后才延迟读取。
- 误以为 404 是成功：`completed` 描述处理链返回方式，业务结果仍看状态码与业务语义。

## 面试与复习题

1. SLF4J 与 Logback 分别是什么？
2. 为什么用参数化日志，而不是字符串拼接？
3. 为什么不能直接信任请求头中的请求 ID？
4. MDC 为什么需要在 `finally` 中移除？为什么不直接清空全部上下文？
5. 为什么同步请求中 ID 正常，不代表异步任务也能拿到它？
6. HTTP 404 与未处理异常在本例的完成日志中有什么区别？

## 本章总结

有用的日志需要稳定字段、合理级别、可关联的上下文与明确的数据边界。日志能辅助定位，但不能替代事务、测试或权限检查。

## 下一步

上一章：[Spring Boot JDBC 与事务](./05-spring-boot-jdbc-transactions.md)

继续按照[学习路线](../../roadmap/index.md)进入安全基础等后续主题。

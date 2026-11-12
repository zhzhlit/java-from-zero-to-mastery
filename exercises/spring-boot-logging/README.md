# Spring Boot 日志与请求追踪练习

使用现有 Web starter 自带的 SLF4J 与 Logback，为同步 Servlet 请求增加关联日志。不新增日志依赖。

## 练习目标

- 通过参数化日志与 `logging.level` 区分 INFO、DEBUG。
- 校验 `X-Request-Id`，缺失或非法时生成 UUID，并写入响应头。
- 用 MDC 把业务日志和请求完成日志关联起来。
- 在正常与异常路径记录耗时，清理 MDC，不吞掉异常。
- 不在应用日志中记录原始 URL、查询参数、正文、Cookie 或认证头。

## 分级任务

- 基础：保留 Controller 的参数化日志，通过配置切换日志级别。
- 进阶：实现 `RequestLoggingFilter`，请求 ID 只允许 1 到 64 个 ASCII 字母、数字、下划线和短横线。
- 挑战：在 `finally` 中记录状态、耗时和完成标记，再仅移除自己的 MDC 键；异常必须继续向上传播。

`starter` 提供可编译过滤器骨架、完整 Controller 与配置，8 个测试在类级 `@Disabled` 下禁用；`solution` 提供完整实现和同样的 8 个测试。完成后移除 starter 测试类上的 `@Disabled`。

## 验证

```bash
mvn -B -pl exercises/spring-boot-logging/starter -am test
mvn -B -pl exercises/spring-boot-logging/solution -am test
```

预期：starter 跳过 8 项；solution 通过 8 项，覆盖关联、ID 边界、404、未处理异常、线程复用、敏感输入和日志级别。

## 运行

```bash
mvn -B -pl exercises/spring-boot-logging/solution -am compile
mvn -B -f exercises/spring-boot-logging/solution/pom.xml org.springframework.boot:spring-boot-maven-plugin:3.3.7:run
```

```bash
curl -i -H 'X-Request-Id: demo-001' http://localhost:8080/courses
curl -i http://localhost:8080/courses/99
```

第一条返回 `200` 与 `X-Request-Id: demo-001`，业务日志和完成日志共享该 ID；第二条返回 `404` 与生成的 ID。示例完成日志：

```text
event=http_request method=GET status=200 durationMs=3 completed=true
```

耗时不固定。ID 位于日志前缀的 `[requestId=...]`，可在控制台搜索。临时调试可在启动命令后追加 `-Dspring-boot.run.arguments=--logging.level.io.github.javamastery.exercises.springbootlogging=DEBUG`。

本练习仅针对同步请求，不涵盖异步 Servlet、线程池 MDC 传播、分布式 tracing、访问日志或生产日志采集。请求 ID 不是认证信息，也不保证客户端提供的 ID 唯一。未处理异常的 `status=500 completed=false` 是过滤器处的失败分类，不保证等于容器最终输出的状态。

---
title: 企业级开发综合复盘
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 企业级开发综合复盘

## 学习目标

- 将配置、分层、JDBC 事务、安全和请求日志组合成可验证的业务链路。
- 区分请求身份、授权规则、业务规则与数据库约束。
- 验证失败路径无写入、事务失败能恢复状态、数据归属正确。

## 前置知识

已完成 [JDBC 与事务](./05-spring-boot-jdbc-transactions.md)、[日志与请求追踪](./06-spring-boot-logging.md)和 [Spring Security 基础](./07-spring-boot-security.md)。本章沿用 Java 21、Spring Boot 3.3.7 与现有依赖，不增加新框架。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/enterprise-basics-review/starter)：报名事务与安全链骨架。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/enterprise-basics-review/solution)：独立可运行实现与 12 项测试。

```bash
mvn -B -pl exercises/enterprise-basics-review/starter -am test
mvn -B -pl exercises/enterprise-basics-review/solution -am test
```

starter 初始跳过 12 项，完成实现后移除测试类上的 `@Disabled`。启动前按 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/enterprise-basics-review#readme) 设置本地密码，不提供默认凭证。

## 一次报名经过哪些边界

1. 请求日志过滤器校验或生成 `X-Request-Id`，设置 MDC。
2. Spring Security 检查登录会话、STUDENT 角色与 CSRF。
3. Controller 从 Principal 取报名人，只负责 HTTP 边界。
4. Service 开启事务，确认课程存在并组织两次写入。
5. Repository 条件扣减名额，随后插入报名记录。
6. 数据库以非负检查、外键和唯一约束维护最后的数据边界。
7. 事务提交或回滚后返回响应；日志过滤器记录状态和耗时，清理 MDC。

本章将请求过滤器设为 `Ordered.HIGHEST_PRECEDENCE`，放在安全链前。否则一个已被安全链拒绝的请求，可能根本不会进入后置过滤器，导致没有关联 ID。日志仍不记录请求正文、查询参数或认证头。

## 身份与数据归属

与 JDBC 单项练习不同，报名接口不再相信请求体中的 student：

```java
public EnrollmentDto enroll(long courseId, Principal principal) {
    return service.enroll(courseId, principal.getName());
}
```

这是 Controller 的流程摘录，完整 HTTP 映射见 solution。身份已经由认证系统确定，不应再次把用户名转小写或去空格后当成另一个用户。本例固定账户名为 student、admin。

`GET /enrollments` 用当前身份作为参数化查询条件；`GET /admin/enrollments` 才允许管理员读取全部。前端隐藏管理员入口并不能替代服务端授权。

## 安全失败和业务失败不同

| 场景 | 本例结果 | 数据库结果 |
| --- | --- | --- |
| 匿名报名，携带有效 CSRF | 302 到登录页 | 不写入 |
| 已登录但没有 STUDENT 角色 | 403 | 不写入 |
| 已登录但缺少 CSRF | 403 | 不写入 |
| 首次合法报名 | 201 | 名额减一，报名数加一 |
| 重复报名且仍有剩余名额 | 409 | 本次扣减回滚，保留上次报名 |
| 课程满员 | 409 | 不写入 |
| 课程不存在 | 404 | 不写入 |

匿名请求若也缺少 CSRF，可能先被 CSRF 过滤器拒绝为 403；不要把响应简单归结为“有没有登录”。

## 事务失败必须观察最终状态

课程 1 初始有 2 个名额。第一次 student 报名提交后，名额为 1、报名数为 1；第二次会先扣名额，再因唯一约束失败。事务必须把第二次扣减恢复，最终仍为 1、1。

Service 使用容器代理的 `@Transactional` 方法，不在测试类或测试方法上另加事务；否则测试事务可能掩盖业务注解缺失。异常由 Service 传播，事务先回滚，再由异常处理器映射为 409，而不是捕获后返回成功。

## 测试组合

12 项集成测试覆盖公开课程、真实密码登录及会话报名、匿名拒绝、CSRF 拒绝、错误角色、伪造学生字段、唯一约束回滚、满员、缺失课程、个人查询、管理员查询及请求 ID 清理。

其中安全拒绝会检查数据库没有变化；回滚会检查名额和报名数；数据归属会以不同身份分别查询。HTTP 状态码只是证据之一，不等于业务正确性。

前几章的细项测试仍保留在独立模块中；本章关注组合边界，不把它当作压力测试、生产安全审计或外部数据库认证。

## 分级练习

- **基础**：画出请求到数据库的调用链，确认 Controller 不直接使用 JdbcTemplate。
- **进阶**：完成报名 Service，建立包含两次写入的事务边界。
- **挑战**：完成安全链，验证角色、CSRF、个人查询边界及被拒绝请求的请求 ID。

## 常见错误与排查

- 被拒绝请求没有 ID：检查过滤器顺序，确认它位于安全链之前。
- 报名人可被伪造：检查是否仍从请求体读取 student，而不是 Principal。
- 重复报名后名额少了一次：检查事务代理、异常传播和测试级事务。
- 学生查询出其他人记录：检查查询条件是否绑定当前身份，不能直接返回全部报名。
- Controller 400/404 变成 403：检查 ERROR 分派权限，不要通过全路径放行修复。

## 面试与复习题

1. 为什么日志过滤器要考虑安全链顺序？
2. 登录、角色、CSRF、业务规则、唯一约束分别负责什么？
3. 为什么已经有角色控制仍要按当前身份过滤查询？
4. 用什么数据证明重复报名确实回滚？
5. 本章距离生产部署还缺哪些能力？

## 本章总结

组合能力不是把注解堆在一个方法上，而是让每个边界独立生效，并用最终数据状态验证成功与失败。至此，企业级开发基础已有一条可运行的综合链路。

## 下一步

上一章：[Spring Security 基础](./07-spring-boot-security.md)

下一章：[缓存基础](../06-middleware-distributed/01-cache-basics.md)，进入中间件与分布式阶段。

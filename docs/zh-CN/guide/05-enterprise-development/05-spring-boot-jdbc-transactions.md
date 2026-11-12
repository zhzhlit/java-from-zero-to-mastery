---
title: Spring Boot JDBC 与事务
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# Spring Boot JDBC 与事务

## 学习目标

- 理解 DataSource、JdbcTemplate、Repository 与事务管理器各自的职责。
- 能通过参数化 SQL 查询并修改 H2 中的数据。
- 能把一次业务中的两次写入作为整体提交或回滚。
- 能用数据库最终状态验证事务，而不只判断 HTTP 状态码。

## 前置知识

已完成 [JDBC 基础](../04-database-web/02-jdbc-basics.md)和 [Spring Boot 分层架构](./04-spring-boot-layered-architecture.md)。本章沿用 Controller → Service → Repository 的调用方向，把内存 Map 换成数据库。

技术版本沿用仓库基线：Java 21、Spring Boot 3.3.7、H2 2.3.232。不引入 JPA、MyBatis、外部数据库或分布式事务。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-jdbc-transactions/starter)：Repository 与事务流程骨架。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-jdbc-transactions/solution)：完整参考实现和 9 个集成测试。

```bash
mvn -B -pl exercises/spring-boot-jdbc-transactions/starter -am test
mvn -B -pl exercises/spring-boot-jdbc-transactions/solution -am test
```

starter 初始跳过 9 个测试；完成实现并移除测试类的 `@Disabled` 后，应全部通过。

## 从自动配置到数据库

模块增加 `spring-boot-starter-jdbc`，并将已有 H2 依赖设为 `runtime`，这样应用启动时也有驱动可用。Spring Boot 为嵌入式数据库创建 DataSource、连接池、JdbcTemplate 和 JDBC 事务管理器。

DataSource 提供连接；JdbcTemplate 管理常规 JDBC 资源与异常转换；Repository 封装 SQL；Service 界定业务事务。Controller 不操作连接，也不自己提交或回滚。

`application.properties` 为每个应用上下文生成独立数据库名，并指定初始化文件与 UTF-8 编码：

```properties
spring.datasource.generate-unique-name=true
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql
spring.sql.init.encoding=UTF-8
```

嵌入式数据库启动时先执行 `schema.sql` 建表，再执行 `data.sql`。课程 1 有 2 个名额，课程 2 没有剩余名额。初始化失败会阻止启动，不应通过忽略错误继续运行。初始化规则见 [Spring Boot 3.3 官方文档](https://docs.spring.io/spring-boot/3.3/how-to/data-initialization.html)。

H2 内存库是真实的关系型 SQL 引擎，支持约束和事务；但它并非持久化部署方案，进程重启后数据会丢失。切换到外部数据库时还需要驱动、连接配置、迁移与该数据库上的验证。

## 表结构与参数化 SQL

`courses` 保存课程及剩余名额，`enrollments` 保存课程 ID 和学生标识。数据库约束保证：

- 课程 ID 是主键，报名必须引用存在的课程。
- 剩余名额不能小于 0。
- 同一课程的同一学生只能有一条报名记录。

查询使用 RowMapper 把行映射为 `CourseDto`。写入时传值，不拼接 SQL：

```java
jdbc.update("INSERT INTO enrollments (course_id, student) VALUES (?, ?)", courseId, student);
```

例如 `o'hara` 应是一个普通参数，不能改变 SQL 结构。学生标识在 Service 中校验长度、去除首尾空格并按 `Locale.ROOT` 转小写，它只是练习用键，不代表已经完成用户认证。

## 一次报名，两次写入

报名先扣减名额，再插入报名记录。扣减必须是数据库中的条件更新：

```sql
UPDATE courses SET available_seats = available_seats - 1
WHERE id = ? AND available_seats > 0
```

根据影响行数判断是否成功。不要先把名额读到 Java 中减一，再无条件覆盖数据库；并发时可能覆盖其他请求的更新。本章验证容量边界，不把这些测试当成多数据库并发压测。

Service 的事务边界覆盖两次写入：

```java
@Transactional
public EnrollmentDto enroll(long courseId, String student) {
    // 校验与规范化、确认课程存在后执行以下步骤。
    if (!courses.reserveSeat(courseId)) {
        throw new IllegalStateException("课程名额已满");
    }
    courses.insertEnrollment(courseId, normalized);
    return new EnrollmentDto(courseId, normalized);
}
```

上面是流程摘录，完整方法见 solution。重复报名时，第一条 UPDATE 可以成功，但第二条 INSERT 触发唯一约束，JdbcTemplate 转换为 `DuplicateKeyException`。异常离开事务方法，Spring 回滚本次扣减。上一笔已经提交的报名保持不变。

## 事务生效的条件

- 通过 Spring 容器注入的 Service 代理调用事务方法，不是手工 `new CourseService(...)`。
- 两次操作都使用同一个 DataSource 下由 Spring 管理的连接。
- 默认代理模式中，同类内部直接调用事务方法不会经过代理。
- 默认情况下，未捕获的运行时异常和 Error 触发回滚；受检异常通常需要显式配置 `rollbackFor`。
- 不要捕获数据库异常后返回成功，否则可能破坏失败语义。这里由事务代理先回滚，再由 ControllerAdvice 映射错误。

代理与回滚语义参考 [Spring Framework 官方事务说明](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)。不要把后续框架版本新增的配置视为本项目 3.3.7 基线功能。

## API 与可观察结果

按 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-jdbc-transactions#readme) 启动应用后：

```bash
curl http://localhost:8080/courses
curl -i -H 'Content-Type: application/json' -d '{"student":"alice"}' http://localhost:8080/courses/1/enrollments
```

- 首次报名：`201`，名额由 2 变为 1，报名数为 1。
- 再次报名：`409`，名额仍为 1，报名数仍为 1。
- 名额不足：`409`，不写入报名记录。
- 课程不存在：`404`；学生标识为空或过长：`400`。

本练习不承诺“重复报名”与“满员”同时成立时的错误优先级；由于先扣名额，满员时优先返回名额不足。数据库原始异常信息不会直接返回给调用方。

## 分级练习

- **基础**：用 JdbcTemplate 完成列表、单条查询和 RowMapper。
- **进阶**：实现条件 UPDATE 和参数化 INSERT，处理满员及输入边界。
- **挑战**：建立 Service 事务边界，让重复报名失败时恢复名额，并通过 HTTP 与直接调用两条路径验证。

## 如何验证回滚

测试使用 `@SpringBootTest` 注入真实 Service 和 JdbcTemplate，不在测试类或方法上加 `@Transactional`。否则测试自己的事务可能替代待验证的 Service 边界，导致去掉业务注解后测试仍误判通过。

先提交一次报名，再制造重复报名失败。失败之后重新查询数据库，确认名额为 1、报名数为 1。只断言抛异常或 HTTP `409`，不能证明回滚成功。

## 常见错误与排查

- `No suitable driver`：检查 H2 是否在运行时 classpath，不要只设为 test。
- `Table not found`：检查 resources 文件是否打包，初始化路径与 SQL 错误是否被忽略。
- 第二次写入失败但名额减少：检查 Service 是否由容器注入、方法是否经过代理、异常是否被吞掉。
- 测试之间相互影响：每个测试前清理报名并恢复名额，不依赖测试执行顺序。
- 把 H2 测试通过等同于生产数据库兼容：还需在目标数据库验证 SQL、约束和事务行为。

## 面试与复习题

1. JdbcTemplate 与原生 JDBC 各自负责什么？
2. 为什么扣名额需要检查 UPDATE 的影响行数？
3. 为什么唯一约束仍然必要，不能只靠先查询？
4. 事务代理何时提交、何时回滚？同类内部调用有什么限制？
5. 为什么集成测试不加测试级 `@Transactional`？
6. H2 内存库与内存 Map、生产数据库分别有什么不同？

## 本章总结

分层解决职责问题，数据库约束维护数据边界，事务保证本次业务的多次写入共同成功或共同失败。验证时要看数据库最终状态，而不只看接口响应。

## 下一步

上一章：[Spring Boot 分层架构](./04-spring-boot-layered-architecture.md)

下一章：[Spring Boot 日志与请求追踪](./06-spring-boot-logging.md)，为同步请求建立可检索的日志上下文。

# Spring Boot JDBC 与事务练习

在分层架构之后，用 `JdbcTemplate` 和 H2 数据库实现课程报名。

## 练习目标

- 通过 Spring Boot 自动配置获得 DataSource、JdbcTemplate 和 JDBC 事务管理器。
- 用 `schema.sql`、`data.sql` 初始化两张关联表。
- 用参数化 SQL 查询课程、原子扣减名额、写入报名记录。
- 在 Service 的公开方法上建立事务边界，观察唯一约束失败后名额恢复。
- 测试通过 Spring 容器中的 Service 代理调用，不使用测试级事务掩盖提交结果。

## 目录与任务

- `starter`：可编译骨架，Repository 和报名流程抛出练习提示异常；9 个测试在类级 `@Disabled` 下禁用。
- `solution`：完整参考实现，同样的 9 个测试默认执行。
- 基础：实现课程列表和按 ID 查询，保留参数占位符。
- 进阶：实现条件扣减与报名写入，校验并规范化学生标识。
- 挑战：添加 `@Transactional`，使重复报名导致的第二次写入失败回滚第一次扣减。

Controller、DTO、异常映射和 SQL 初始化脚本已提供。完成 starter 后移除测试类上的 `@Disabled`。

## 验证

```bash
mvn -B -pl exercises/spring-boot-jdbc-transactions/starter -am test
mvn -B -pl exercises/spring-boot-jdbc-transactions/solution -am test
```

预期：starter 的 9 个测试跳过；solution 的 9 个测试通过，覆盖查询、提交、约束失败回滚、HTTP 错误映射、容量边界、缺失课程、输入校验和 SQL 参数绑定。

## 运行 API

在仓库根目录执行：

```bash
mvn -B -pl exercises/spring-boot-jdbc-transactions/solution -am compile
mvn -B -f exercises/spring-boot-jdbc-transactions/solution/pom.xml org.springframework.boot:spring-boot-maven-plugin:3.3.7:run
```

另一个终端中调用：

```bash
curl http://localhost:8080/courses
curl -i -H 'Content-Type: application/json' -d '{"student":"alice"}' http://localhost:8080/courses/1/enrollments
```

第一次报名返回 `201`，课程 1 名额从 2 减至 1；再次提交相同学生返回 `409`，名额仍为 1。
学生标识仅用作练习中的报名键，不代表登录身份；去除首尾空格后按 `Locale.ROOT` 转小写。

本模块使用真实 SQL 引擎的 H2 内存库，不是 Java Map，但重启后数据不保留；不连接生产数据库、不演示认证或数据库迁移。

---
title: 事务提交后缓存失效
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 事务提交后缓存失效

## 学习目标

- 区分方法返回、数据库事务提交与 Redis 失效三个时间点。
- 将失效绑定到实际提交，加入外层事务时等待外层结果。
- 验证回滚不失效、活动事务不回填共享缓存。
- 正确认识提交后 Redis 故障，避免把调用异常当成数据库回滚。

## 前置知识

已完成 [Redis 共享缓存](./02-redis-cache.md)和 [JDBC 与事务](../05-enterprise-development/05-spring-boot-jdbc-transactions.md)。本章扩展已有 `exercises/redis-cache`，不重复复制一个模块；增加现有技术基线中的 Spring JDBC 与 H2 依赖。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache/starter)：补齐 `TransactionalCourseService`、`AfterCommitInvalidator` 与 Catalog 的事务内读取。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache/solution)：完整实现、9 项新增默认事务测试、4 项新增真实 Redis 事务测试。

```bash
mvn -B -pl exercises/redis-cache/solution -am test
mvn -B -pl exercises/redis-cache/solution -am verify -Predis-it -Dredis.port=16389
```

模块还包含后续 Outbox 扩展，solution 默认共 30 项测试，Redis profile 再执行 14 项集成测试。starter 对应测试初始禁用，完成后移除标记。专用 Redis 启动和演示命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache#readme)。

## 为什么写源后立即删除还不够

上一章的内存源更新立即可见。换成数据库后，UPDATE 执行完不代表外层事务已经提交。如果此时删除缓存，其他连接可能回源读到旧的已提交值，再把它缓存起来。

所以数据库更新使用新的 `TransactionalCourseService.rename`，而不是基础内存演示的 `RedisCourseCatalog.rename`。Service 使用 Spring 的 TransactionTemplate：

```java
return transactions.execute(status -> {
    Course updated = source.rename(id, title);
    invalidator.invalidateAfterCommit(id);
    return updated;
});
```

这里采用编程式事务来显式演示边界；不是手写事务引擎，也不要求自行调用 Connection.commit。其默认 REQUIRED 传播行为会加入已有事务，不是每次服务方法返回都单独提交。

TransactionTemplate 的事务管理器与 Repository 的 JdbcTemplate 必须使用同一个 DataSource。线程上存在其他数据源的事务，不足以证明当前 SQL 已经加入该事务；本章测试和示例明确绑定同一数据源。

## afterCommit 与实际事务

失效器同时检查实际事务和同步机制已激活，再注册 TransactionSynchronization，在 `afterCommit` 中删除相应 Redis 键。没有实际事务时直接拒绝，不悄悄退化为立即删除。

回调结果：

- 没有外层事务：Service 创建事务，成功提交后失效。
- 有外层事务：Service 返回时先保留缓存，外层成功提交后失效。
- 外层抛异常或标记 rollback-only：数据库回滚，不执行 afterCommit。
- SQL 约束失败：异常传播，数据库不提交，也不删除缓存。

注意 afterCommit 不等于 afterCompletion：后者在提交或回滚后都可能调用。异常传播语义也不同，不能随意替换。本例使用的具体行为见 [Spring 6.1.16 TransactionSynchronization API](https://docs.spring.io/spring-framework/docs/6.1.16/javadoc-api/org/springframework/transaction/support/TransactionSynchronization.html#afterCommit())。

## 活动事务内绕过共享缓存

只延后删除还不够。如果本事务 UPDATE 后调用 Catalog 查询，JDBC 连接能读到自己的未提交数据。此时回填 Redis，就会让外部请求看到尚未提交的标题，即使随后回滚也可能残留脏值。

本章采用明确的小范围策略：只要 Spring 检测到活动事务，Catalog 就直接读源，不 GET Redis，也不 SET 回填；事务外仍按 cache-aside 读取。这个策略同时避开事务快照读取污染共享缓存的问题，代价是事务内不能享受缓存命中。

SQL 的隔离语义仍由数据库负责；这不是分布式一致性或跨线程事务上下文传播方案。

## 提交后失败不能再回滚数据库

afterCommit 开始时数据库已经提交。Redis DEL 失败，异常会传回提交调用者，但不会撤销之前的数据库提交。

本章真实测试关闭执行失效的客户端连接池，再让外层事务提交，验证：

1. 调用方收到异常。
2. 数据库保存新标题。
3. 另一个正常客户端仍读到 Redis 旧标题。

这不是成功，也不是数据库回滚，而是跨组件的部分完成。不要对这种异常盲目重试整个业务操作；生产需要可观测性、幂等性与可靠失效记录或补偿。

回调只做 Redis 删除，不继续写原 JDBC 事务中的业务数据。若回调需要新的数据库写入，应另行设计独立事务；不能假设已经提交的事务还会再提交一次。

## 测试如何避免假通过

默认新增 9 项测试使用真实 H2 与模拟 Redis，验证独立提交、等待外层提交、异常回滚、rollback-only、唯一约束失败、实际事务检查、失效异常、事务内读取和回调不串到下一事务。

新增 4 项 Redis 集成测试使用 H2 和两个真实客户端，检查提交前共享键仍为旧值、提交后失效重载、回滚保留原值、约束失败和失效连接故障的最终状态。测试不通过测试级 `@Transactional` 自动回滚，而是显式结束事务。

这些测试证明具体时序，不证明所有并发情况。事务外的旧值加载仍可能与提交失效竞争；进程在数据库提交后、回调执行前崩溃，也可能漏掉删除。

## 分级练习

- **基础**：阅读 JDBC Repository 与表结构，理解唯一约束失败。
- **进阶**：用 TransactionTemplate 更新源，并在实际事务中注册 afterCommit。
- **挑战**：完成活动事务内的缓存绕过，分别验证外层提交、回滚与失效故障。

## 常见错误与排查

- Service 返回后缓存就被删了：检查是否仍调用基础 Catalog.rename，或没有加入实际外层事务。
- 回滚仍删除缓存：检查是否误用 finally 或不区分状态的 afterCompletion。
- 回滚后 Redis 还有未提交值：检查事务内读取是否回填了共享缓存。
- 收到异常就认为数据库没更新：重新查询已提交数据，并区分失败发生在提交前还是提交后。
- 无事务也能注册成功：同步机制开启不等于实际数据库事务已开启，需要分别检查。

## 面试与复习题

1. Service 方法返回与最外层事务提交有什么区别？
2. afterCommit 与 afterCompletion 为什么不能直接互换？
3. 为什么活动事务内不应该把自己的新值写进共享缓存？
4. afterCommit 的 Redis 异常能否撤销数据库提交？
5. 本章还没有解决哪些崩溃窗口和并发竞态？

## 本章总结

提交后失效修正了数据库提交与缓存删除的先后关系，但不等于跨组件原子提交。验证时必须同时观察事务结果、数据库与 Redis，而不是只看调用是否抛异常。

## 下一步

上一章：[Redis 共享缓存](./02-redis-cache.md)

下一章：[可靠失效与失败恢复](./04-cache-invalidation-outbox.md)，将需要恢复的失效动作记录到数据库。

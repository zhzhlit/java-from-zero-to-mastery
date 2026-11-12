---
title: 可靠失效与失败恢复
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 可靠失效与失败恢复

## 学习目标

- 理解数据库提交后回调仍有进程退出或缓存服务故障窗口。
- 将业务变更与失效任务放在同一数据库事务中。
- 实现有界处理、失败可见、保留待处理与幂等重放。
- 区分数据库任务持久化、至少一次执行与强一致性。

## 前置知识

已完成 [事务提交后缓存失效](./03-cache-commit-invalidation.md)。本章继续扩展已有 `exercises/redis-cache`，复用 Spring JDBC、H2 和 Jedis，不新增依赖，也不改掉前几章的独立演示。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache/starter)：完成 OutboxCourseService、InvalidationOutbox 与 OutboxDispatcher。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache/solution)：完整实现，新增 11 项默认测试及 4 项真实 Redis 测试。

```bash
mvn -B -pl exercises/redis-cache/solution -am test
mvn -B -pl exercises/redis-cache/solution -am verify -Predis-it -Dredis.port=16389
```

包含前几章后，模块共 30 项默认测试、14 项 Redis 集成测试。starter 对应测试类初始禁用，完成后移除标记。专用 Redis 与恢复示例的运行命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache#readme)。

## 为什么还需要任务表

afterCommit 修正执行时序，但回调只在当前进程中。如果数据库已经提交，而进程在删除缓存前退出，就可能漏掉失效。Redis 故障时，调用者也不能把已提交的业务更新当成未发生。

本章将“需要删除哪个缓存键”记录到数据库：课程 UPDATE 与任务 INSERT 共同提交或回滚。业务返回代表课程及任务已提交，不代表缓存已经删除；另一个处理器稍后执行任务。

这个表通常称为 Outbox。本例不把 Outbox 等同于完整消息队列，也没有声称实现了跨 Redis 与数据库的原子事务。

## 同一事务写入两种状态

```java
return transactions.execute(status -> {
    Course updated = source.rename(id, title);
    outbox.enqueue(id, key);
    return updated;
});
```

Repository、Outbox 和事务管理器必须使用同一 DataSource。只检查线程上有事务，不足以证明任意数据源的写入都在其中；本章 fixture 和示例明确绑定相同数据源。

- 更新成功且任务写入成功：两者提交，不调用 Redis。
- 任务写入失败：课程更新也回滚。
- 外层事务回滚：任务随课程变更一起回滚。
- SQL 约束失败：不留下待执行任务。

Spring TransactionTemplate 的生命周期参考 [6.1.16 API](https://docs.spring.io/spring-framework/docs/6.1.16/javadoc-api/org/springframework/transaction/support/TransactionTemplate.html)。

## 任务保存什么

任务记录数据库 ID、课程 ID、完整缓存键、是否完成、已记录的尝试计数和固定错误码。实际键由服务端 Catalog 生成，不来自客户端请求。

保存完整键是为了让重建的 worker 不依赖旧进程的随机命名空间。不要在恢复时随意生成新命名空间，否则任务可能执行了，却没有删除原来那一份旧缓存。

错误码使用稳定值 `REDIS_DELETE_FAILED`，不把原始异常消息或凭证写入表中。原始 Redis 异常通过处理结果返回给内部调用方，调用方需要检查失败明细，不能忽略结果而宣称成功。

## 有界处理与失败语义

处理器在业务事务外读取最多 1 到 1000 条待处理任务，每次只遍历一次选出的批次：

1. DEL 任务中保存的缓存键。
2. 成功后更新该任务为已完成。
3. Redis 失败则保留待处理、记录失败，并继续本批其他任务。
4. 数据库确认或失败记录写入异常直接抛出，不能假装状态已保存。

待处理查询优先较少尝试的任务，再按 ID 排序，避免一个失败任务总占据小批次。它不保证持续高流量下的公平性，也没有自动退避与无限重试循环。

处理器方法的 synchronized 只约束同一对象。本章要求单 worker，不提供跨进程任务抢占或租约；不能把它解释成分布式锁。

## 为什么允许重复删除

Redis 删除成功但数据库确认失败时，任务仍待处理；下次再删同一键是合理的。不存在的键也可安全执行 DEL，见 [Redis DEL 文档](https://redis.io/docs/latest/commands/del/)。因此本例采用可重放的失效操作，不声称 exactly-once。

重复删除也可能再次清除刚回填的新值，导致额外回源，但不会写入旧标题。注意这不是说整个“课程更新”也幂等，更不是解决了所有并发回填竞态。

## 恢复与证据

新增默认测试使用真实 H2 与模拟 Redis，覆盖同事务提交、外层回滚、约束失败、任务写入失败、成功确认、Redis 故障可见、稍后重试、确认失败重放、有界批次、事务边界以及文件库关闭重开。

文件型 H2 测试只初始化一次，提交后关闭数据库，再以新连接和新 Outbox 对象读取原任务并处理；没有重新插入任务。这证明任务位于文件存储，不只是旧 Java 对象还在内存中。它仍不是完整的进程崩溃、断电或生产数据库灾备验证。

新增 4 项真实 Redis 测试验证：提交后旧值直到处理才失效；失效连接池故障后新 worker 恢复；回滚没有任务且保留旧缓存；同键多条任务可重复执行并最终加载最新标题。

## 分级练习

- **基础**：实现任务的参数化插入、查询和状态更新，校验影响行数。
- **进阶**：完成业务与任务的同事务写入，验证任务写入失败会回滚课程。
- **挑战**：实现批次处理和失败明细，验证删除后确认失败的重放以及文件库恢复。

## 常见错误与排查

- 数据更新了但没有任务：检查两个写入是否使用同一实际事务和数据源。
- 先确认再 DEL：进程退出后可能永远丢失删除，不要把顺序倒过来。
- 故障时任务被清掉：失败应保留待处理，不应标记为完成。
- 重建 worker 后删错缓存：检查是否使用任务保存的实际键。
- 处理器跑在业务事务中：可能读取未提交任务并提前删除，本例直接拒绝这种调用。
- 只看到成功数量就认为全批成功：同时检查 failures 和待处理任务，不忽略异常。

## 面试与复习题

1. afterCommit 与 Outbox 分别解决哪一类问题？
2. 为什么业务更新与任务必须使用同一数据源事务？
3. DEL 已完成而数据库确认失败，下次该怎么办？
4. 为什么应该保存实际缓存键而不是恢复时重新生成？
5. 有了 Outbox 后，为什么缓存仍不是强一致的？
6. 生产 worker 还需要哪些调度、抢占、重试和历史清理能力？

## 本章总结

Outbox 将需要恢复的动作变成数据库状态，再以幂等操作处理至少一次执行。恢复仍依赖可靠数据库、worker 持续运行和故障监控；本章提供显式调用的单 worker 教学实现，不是生产调度平台。

## 下一步

上一章：[事务提交后缓存失效](./03-cache-commit-invalidation.md)

继续按照[学习路线](../../roadmap/index.md)进入消息队列基础。

---
title: Redis 共享缓存
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# Redis 共享缓存

## 学习目标

- 区分本地缓存与独立 Redis 服务的共享范围。
- 用 Jedis 完成 GET、带过期的 SET 和单键 DEL。
- 验证命名空间、TTL、独立客户端共享和异常路径。
- 理解共享缓存并不自动提供强一致性或分布式锁。

## 前置知识

已完成 [缓存基础](./01-cache-basics.md)，理解命中、源数据、TTL 和失效。本章沿用 Java 21，Jedis 5.0.2 来自现有 Boot BOM，真实验证使用 Redis 7.4.12。

客户端版本保持仓库基线，不照搬最新 Jedis 文档中新版 API。源码和实际测试是本章具体版本的依据，概念可参考 [Redis 官方 Jedis 指南](https://redis.io/docs/latest/develop/clients/jedis/)。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache/starter)：GET/回源/SET、写源后 DEL、显式失效骨架。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache/solution)：完整实现、10 项单元测试和 6 项真实集成测试。

```bash
mvn -B -pl exercises/redis-cache/solution -am test
mvn -B -pl exercises/redis-cache/solution -am verify -Predis-it -Dredis.port=16389
```

第二条需要先启动专用 Redis，命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/redis-cache#readme)。starter 两个测试类初始禁用，完成实现后移除标记。

模块还包含后续事务与 Outbox 扩展：默认共 30 项测试，Redis profile 共 14 项集成测试。下面的 10 项与 6 项分别描述本章基础测试；活动事务内读取会按下一章规则直接读源，不访问共享缓存。

## 从 JVM 内存到共享服务

上一章 Caffeine 位于当前 JVM，其他进程不能直接读取同一份缓存。Redis 是独立服务，客户端通过网络访问同一键空间。本例创建两个独立 JedisPooled 客户端和两个 Catalog 对象：第一次读取回源，第二个客户端读取相同键时命中 Redis，源查询数仍为 1。

这证明独立连接共享 Redis 值，不代表已验证真实多 JVM 部署、网络分区或高可用。两个 Catalog 使用同一个模拟数据源，以避免把源数据差异混入基础缓存演示。

## 键与值的边界

键形如 `learning:redis:v1:course:1`。命名空间由调用方指定，限制为安全字符并以冒号结尾；同一公共课程的同一数据域应共用命名空间，不同测试或独立数据域应隔离。

本例值只保存课程标题，ID 来自键，不手拼 JSON，也不使用 Java 原生对象序列化。若以后增加字段，应采用明确的结构化序列化格式与版本策略，而不是随意拼接分隔符。

不要仅凭命名空间认为已经实现租户授权。私有数据还需要服务端权限检查和正确的作用域。本章不缓存身份、密码、权限或报名名额。

## 读缓存与带过期写入

读取先 GET，命中时构造不可变 Course；未命中再查源。源存在时，使用单条 SET 配置毫秒过期：

```java
redis.set(key, value.title(), SetParams.setParams().px(ttlMillis));
```

不要先 SET 再单独设置过期；两条命令之间失败，可能留下没有 TTL 的值。本例缺失值不写入 Redis，异常继续传播，未引入负缓存。

与 Caffeine 的单实例原子加载不同，这个 GET → 查源 → SET 流程不会合并不同客户端的同键请求，并发 miss 仍可能多次回源。没有使用 SET NX，就不应声称实现了分布式锁。

## 共享失效不等于一致性完成

更新先写源，成功后 DEL 同一命名空间中的课程键。另一客户端下一次查询 miss 后重载，因此顺序更新能被观察到。

但多步操作不是整体原子的：

- 一个客户端可能在更新前读到旧源值，更新后又把旧值回填。
- 数据库事务未提交前删缓存，另一个请求可能把未更新的已提交值重新缓存。
- 源已更新但 DEL 失败，调用者会收到异常，旧值仍可能存在到 TTL。
- Redis 不可用时，本例直接报错，没有静默回源或自动重试。

这些边界应体现在接口语义、监控和恢复设计中，不能简单捕获异常后假装更新全部完成。后续再学习提交后失效与一致性策略。

## TTL、淘汰与清理

TTL 限制单键有效时间，并不限制整个 Redis 的内存。练习启动命令同时设置专用实例的 maxmemory 和淘汰策略，避免把有限时间误当成无限容量的解决方案。

真实测试先用 PTTL 检查 1 分钟 TTL 已设置，再通过服务器 PEXPIRE 立即到期，验证下一次重载新值。这个测试验证服务器过期及回源，不模拟长时间等待，也不宣称覆盖所有自然过期调度时机。

集成测试每次使用随机命名空间，只 DEL 已知测试键。不要使用全库清空命令清理共享开发环境。

## 两层验证证据

10 项 Mockito 单元测试验证命中不读源、未命中调用、缺失不缓存、源错误、Redis 错误、写入与失效顺序、失效失败可见以及参数校验。它们不证明 Redis 行为。

6 项 Failsafe 集成测试连接真实 Redis，验证两个客户端共享、共享失效、缺失值、TTL 和服务器过期、命名空间隔离及源写入失败。使用 `redis-it` profile，服务缺失时应失败，不自动跳过。CI 也运行该 profile。

## 分级练习

- **基础**：完成 GET 与未命中回源，观察两个客户端的源查询次数。
- **进阶**：实现 SET PX 和写源后 DEL，检查实际 PTTL。
- **挑战**：跑真实集成测试，解释失效失败为何可能出现“源已改、调用仍失败”。

## 常见错误与排查

- Connection refused：确认专用服务、地址和端口，不修改为生产地址尝试。
- 键永久存在：检查是否将 TTL 与 SET 放在同一条命令中。
- 不同练习互相污染：使用独立命名空间，避免泛用键名。
- 同键仍多次查源：本例没有跨客户端加载合并，不是库自动保证的行为。
- 更新后仍读旧值：区分顺序失效成功、并发回填、事务提交和失效失败，不凭猜测添加重试。

## 面试与复习题

1. Caffeine 与 Redis 的共享范围有什么不同？
2. 为什么把 SET 与 TTL 放在一个命令中？
3. 为什么 Redis 共享失效仍不等于强一致性？
4. Redis DEL 失败时，源数据是否一定没有更新？
5. TTL 与最大内存限制有什么不同？
6. Mockito 与真实服务测试各自提供什么证据？

## 本章总结

共享缓存把额外状态放到网络服务中，除了命中，还要考虑命名空间、资源边界、失败语义与一致性。先用真实服务验证基础命令，再讨论事务提交和跨组件恢复。

## 下一步

上一章：[缓存基础](./01-cache-basics.md)

下一章：[事务提交后缓存失效](./03-cache-commit-invalidation.md)，验证外层事务与提交后故障。

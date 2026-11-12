---
title: 缓存基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 缓存基础

## 学习目标

- 区分数据源与缓存，用命中、未命中和加载次数观察效果。
- 用成熟缓存库实现容量限制、过期、加载和失效。
- 理解缓存并不自动带来一致性，明确单进程示例的适用边界。
- 用可控时钟测试过期，不依赖等待和机器速度。

## 前置知识

已完成 [企业级开发综合复盘](../05-enterprise-development/08-enterprise-basics-review.md)，理解业务数据、事务与授权。中间件与分布式阶段先从本地缓存进入，不立即引入外部服务。

技术基线为 Java 21，Caffeine 版本 3.1.8 由仓库现有 Spring Boot BOM 管理。本模块只使用 Caffeine 与 JUnit，不需要启动 Spring 容器或 Redis。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/cache-basics/starter)：缓存读取、更新失效和显式失效骨架。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/cache-basics/solution)：完整实现及 11 项测试。

```bash
mvn -B -pl exercises/cache-basics/starter -am test
mvn -B -pl exercises/cache-basics/solution -am test
```

完成 starter 后移除测试类的 `@Disabled`。演示运行命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/cache-basics#readme)。

## 数据源不是缓存

本章 `InMemoryCourseRepository` 是模拟事实来源，记录每次查询次数。`CachedCourseCatalog` 在它前面增加本地缓存：第一次查课程 1 读取数据源，第二次同键直接返回缓存值，源查询次数仍为 1。

Course 使用不可变 record，避免调用方修改一个缓存对象后影响其他读取者。这里只缓存公共课程标题，不缓存用户报名名额、密码、身份或授权结果。

不要把“本地缓存”理解为“数据库已经不需要了”；进程重启、过期和淘汰后必须能重新加载源数据。

## 配置与加载

```java
Cache<Long, Course> cache = Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .expireAfterWrite(ttl)
        .ticker(ticker)
        .recordStats()
        .build();
```

最大容量限制条目数，不等于精确的字节上限。TTL 采用写入后过期，普通读取不延长其有效期。过期条目不能继续命中；物理清理由库的维护机制执行，不保证到点立即从内存中删除。

加载使用：

```java
Optional.ofNullable(cache.get(id, key -> source.findById(key).orElse(null)))
```

同一个缓存对象内，同键成功加载由库协调，不手写先查再放的竞态逻辑。返回 null 不建立缓存项，加载异常继续传播；本例因此不缓存缺失课程和失败结果。不同时间反复查询不存在的 ID 仍会访问数据源，这不是已经解决了缓存穿透。

参考 [Caffeine 加载说明](https://github.com/ben-manes/caffeine/wiki/Population)与 [淘汰和过期说明](https://github.com/ben-manes/caffeine/wiki/Eviction)。

## 更新与失效

顺序更新采用“先写源，再失效缓存”：

```java
Course updated = source.rename(id, title);
cache.invalidate(id);
return updated;
```

下一次查询重新读取新标题。如果源更新因校验或课程不存在而失败，不失效原缓存，不伪造成功结果。另有显式失效方法，适合演示已知键刷新。

注意：这里的数据源没有数据库事务，且教学测试的更新链路是顺序的。真实事务中，应考虑提交成功之后再失效；并发读写与跨实例还存在竞态和传播问题，不能把这几行代码解释为强一致性方案。

源数据绕过缓存服务直接修改时，缓存会在 TTL 内暂时返回旧值。这在过期测试中被明确展示，而不是隐藏。

## 命中率不是正确性

开启 `recordStats()` 后可查看命中和未命中次数。本例两次连续读取产生一次 miss、一次 hit；源读取计数用来证明重复读取减少。

高命中率不表示数据一定正确，也不表示内存开销合理。生产需要结合数据新鲜度、加载耗时、内存占用和业务容忍度判断。不要用缓存命中率替代事务、权限或业务状态校验。

## 可重复的测试

测试通过注入 `Ticker` 控制纳秒时间，不在测试里 sleep。先读缓存，再直接改源，推进到第 9 秒仍看到旧值，第 10 秒到期后重新读取新值。

容量维护后检查条目数不超过配置，不断言库一定淘汰哪个键；同键并发读取通过起跑门和有界等待验证只加载一次。空值、异常、非法配置、更新失败及显式失效都有独立覆盖。

## 分级练习

- **基础**：完成原子加载，比较两次查询的源读取计数和统计。
- **进阶**：完成写源后失效与显式失效，验证下一次读取获得新标题。
- **挑战**：解释可控时钟、容量维护、缺失值未缓存和同键并发加载测试。

## 常见错误与排查

- 缓存无限增长：检查容量策略，不把 ConcurrentHashMap 当成完整缓存库。
- 过期测试偶尔失败：不要依赖 sleep，注入 Ticker。
- 改完标题仍旧：检查写入路径是否绕过失效，是否尚未过期。
- 把异常缓存成正常值：让加载异常传播，不返回伪造 Course。
- 同键合并被误当成全局锁：它只针对该缓存实例，不协调其他 JVM。
- 只用课程 ID 缓存私有数据：真实多租户或按用户可见的数据还需要正确的作用域键与授权边界。

## 面试与复习题

1. 缓存与事实来源有何区别？
2. 写入后过期与访问后过期有什么不同？
3. 为什么本例不承诺并发读写或跨实例强一致性？
4. 缺失值不缓存有什么代价？
5. 为什么过期测试不应 sleep？
6. 为什么高命中率仍可能返回错误数据？

## 本章总结

缓存以额外状态换取更少的源查询，必须同时设计容量、过期、失败与失效。先验证这些基础行为，再讨论共享缓存、事务时序与跨实例一致性。

## 下一步

上一章：[企业级开发综合复盘](../05-enterprise-development/08-enterprise-basics-review.md)

继续按照[学习路线](../../roadmap/index.md)进入共享缓存与一致性等后续主题。

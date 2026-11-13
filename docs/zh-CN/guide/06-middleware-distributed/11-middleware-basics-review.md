---
title: 中间件综合实践
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-13"
---

# 中间件综合实践

## 学习目标与入口

- 将权威数据、可靠事件、搜索投影和远程查询连成可验证流程。
- 指出每一步的提交边界，而不是把多个组件当成一个事务。
- 对失败重放、数据库去重和索引恢复给出具体操作。

先完成[可靠发布](./08-outbox-stream-publishing.md)、[搜索基础](./09-search-basics.md)及[RPC 基础](./10-rpc-basics.md)。本章复用三个模块的 solution 作为已完成的基础库；starter 不依赖本章 solution，也不复制引擎代码。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/middleware-basics-review/starter)：完成 release、processBatch 和 backfill，移除对应测试类的 `@Disabled`。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/middleware-basics-review/solution)：完整链路与启用测试。
- [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/middleware-basics-review#readme)：专用 Redis、运行演示与失败窗口表。

```bash
mvn -B -pl exercises/middleware-basics-review/solution -am verify
mvn -B -pl exercises/middleware-basics-review/solution -am verify -Predis-it -Dredis.port=16389
```

本模块有 9 项默认测试、5 项真实 Redis 集成测试；`-am` 还会构建并验证前置模块。默认测试使用真实 H2、Lucene 和模拟 Redis，不能替代 Redis profile。

## 一条课程发布链路

```text
release：课程 + 描述/级别 + Outbox，同数据库事务提交
  -> PublicationRelay：提交后 XADD，再标记发布
    -> 消费：校验事件与权威数据
      -> Lucene upsert -> commit -> refresh
        -> Inbox + 数据库通知，同事务提交
          -> XACK

RPC GetCourse -> 权威数据库
Search -> Lucene 派生索引
```

课程事件只携带 ID、标题和稳定 eventId。描述与级别同课程、Outbox 一起落库，消费时从权威数据读取，不能为了凑齐索引字段随意填默认值。

本章只支持首次发布。ID 重复由主键拒绝；没有改名、删除或多版本更新，因而不声称处理乱序更新。RPC 查询权威源，在消息尚未投递时就能返回课程；搜索必须等待投影，短暂搜索不到不等于课程不存在。

## 为什么不能用 Inbox 回执直接跳过索引

Inbox 的去重记录与数据库通知共享一个本地事务，但不与 Lucene 共享事务。索引文件丢失后，数据库回执可能仍在；看到回执就跳过索引，会让恢复无从发生。

因此每次重放都按课程 ID 重写、提交并刷新索引，再调用 Inbox。重写同一课程不是重复追加文档；Inbox 则避免重复通知行。已有回执只代表数据库副作用已完成，不代表派生索引此刻仍完整。

这里明确选择“索引先于通知”。若索引已提交而通知事务失败，搜索可能先看到课程；因为权威课程已经提交，这个状态可接受，但不能称为整个消费回滚。修复后重放会再次 upsert，再补齐通知。

## 三个可观察的失败窗口

1. **索引失败**：抛出原 IOException，不写数据库回执、不确认消息。修复后读取自己的 pending，或按空闲阈值显式接管。
2. **通知失败**：索引可能已经持久化，数据库通知与回执一起回滚，消息不确认。重放不能删除或忽略已经完成的索引写入。
3. **确认断连**：索引与通知可能均已成功，消息仍 pending。再次处理会重写同 ID 索引，通知返回重复，随后确认。

批次逐条提交，失败前的前缀可能已经成功；不是全批次原子回滚。坏事件、来源不存在或标题冲突会直接停止，不自动确认或吞掉异常。本章未组合自动毒消息隔离，分类与死信策略见[前置课程](./07-retry-dead-letter.md)。

## 索引丢失后的补齐

backfill 不依赖 Stream 是否还保留历史，也不清空 Inbox，而是扫描权威课程，按 ID 写入并提交索引。恢复操作应停止消费并创建新空目录，再补齐、验证和恢复消费。

这不是在线原子切换：旧索引中的多余文档不会自动删除，课程一次性读入内存，没有大规模分页、更新水位或版本管理。索引恢复不需要伪造“通知未完成”，更不能通过删除去重记录来强迫重放，造成重复业务副作用。

## 验证证据

默认测试覆盖同事务写入、Outbox 失败回滚、错误数据源、外层事务、健康流程、索引失败、通知失败、确认失败、坏载荷、补齐和批次上限。

五项 Redis 集成测试进一步验证：

- 真实 H2 -> Outbox -> Redis -> Lucene -> 数据库通知链路，RPC 同时通过真实回环 HTTP/2 查询权威源。
- Redis 键类型故障时课程仍可 RPC 查询，Outbox 保留，修复后无需重新发布课程。
- 索引和通知成功后关闭 Redis 客户端，重读 pending 后只保留一条通知及一个索引文档。
- 索引失败后由另一个消费者显式接管并恢复。
- 文件索引提交后关闭重开，以及已有回执时向新索引补齐并继续消费。

这些是受控故障注入和有序关闭，不是进程崩溃或掉电证明。生产仍需独立设计持久化、容量、监控、身份认证及历史保留。

## 自检与下一阶段

- RPC 查得到但搜索不到，应该先排查哪个提交边界？
- 为什么索引提交后通知失败，不能宣称“本次消费没有产生任何效果”？
- 为什么重放不能简单看到数据库回执就返回成功？
- 为什么补齐索引不应该删除通知回执？

中间件阶段已覆盖缓存、消息、搜索、RPC 和一个组合实践；生产部署仍是后续主题。按[学习路线](../../roadmap/index.md)进入 Java 高级阶段，从并发基础与线程任务开始。

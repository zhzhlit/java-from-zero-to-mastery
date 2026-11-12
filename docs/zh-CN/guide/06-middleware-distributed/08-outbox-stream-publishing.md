---
title: 可靠发布与 Outbox 衔接
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 可靠发布与 Outbox 衔接

## 学习目标

- 把课程业务数据和待发布事件写入同一个数据库事务。
- 将事件 ID、目标 Stream 与载荷快照持久化，提交后显式投递。
- 解释发送成功但确认失败为什么会重复投递。
- 用已有 Inbox 将重复投递收敛为一次数据库业务效果。

## 前置知识与入口

先完成 [可靠失效与失败恢复](./04-cache-invalidation-outbox.md)、[消费者失败恢复与消息幂等](./06-consumer-recovery-inbox.md)和[死信处理](./07-retry-dead-letter.md)。缓存失效的重复 DEL 没有副作用，而重复 XADD 会生成新消息；本章将这一区别落到可验证链路。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/starter)：完成 CoursePublishingService、PublicationOutbox 与 PublicationRelay 的骨架，表结构及事务检查辅助方法已提供。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/solution)：完整实现和 OutboxPublicationDemo，不新增依赖。

```bash
mvn -B -pl exercises/message-queue-basics/solution -am test
mvn -B -pl exercises/message-queue-basics/solution -am verify -Predis-it -Dredis.port=16389
```

本章新增 12 项默认测试、5 项真实 Redis 测试；模块总计 46 项默认、25 项 Redis 集成测试。starter 对应测试类使用 `@Disabled`，完成后移除。专用 Redis 启动与演示命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics#readme)。

## 直接双写为什么不够

```text
先数据库、后 Redis：提交后进程停止，课程存在却没有事件
先 Redis、后数据库：数据库回滚，消费者却已读到不存在的课程
afterCommit 回调：不等于持久任务，回调失败后仍需恢复依据
```

本地 Outbox 让数据库中的业务数据和发送意图一起提交，但不让数据库和 Redis 一起提交。只要待发布任务仍在、投递器恢复执行且目标可用，就有重放依据；不是不依赖运行条件的“绝不丢失”。

## 第一步：固定事件并同事务落库

CoursePublishingService.publish 创建事件 ID，在实际事务内插入 published_courses 与 publication_outbox，不调用 Redis。任何写入失败都会回滚。服务拒绝嵌套业务事务；写入前验证绑定的数据源，避免事务管理器配错导致业务插入自动提交。

Outbox 保存 eventId、destination、courseId、title、published、streamId、attempts 和 errorCode。事件标题是发布时快照，不能重试时重新查询课程当前标题；eventId 也不能每次发送重新生成。目标 Stream 随任务落库，投递器校验队列目标并只扫描自己的目标。

API 请求幂等与事件重投不同：再次调用 publish 会创建新的事件候选；相同课程 ID 被主键约束拒绝，本章没有请求幂等键或“重试自动返回上次结果”。

## 第二步：提交后显式投递

```text
数据库提交业务与 Outbox
  -> relayBatch 读取有界待发布批次
    -> XADD 版本化事件
      -> Redis 返回 Stream ID
        -> 数据库标记 published，保存 Stream ID
```

投递器在业务事务之外运行，不在网络调用期间持有业务事务。批次为 1 到 100，失败次数少的任务优先，然后按数据库序号排列。

Jedis 异常会增加任务失败次数、保留 pending 并在结果中报告原异常，再尝试同批下一条；配置错误或数据库错误直接抛出。失败记账自身出错时，数据库异常的 suppressed 列表保留原 Redis 异常，便于定位两侧故障。

数据库标记成功只表示投递完成，不表示消费成功。消费者可能尚未读取，也可能已经进入死信。发布侧 attempts 与上一章消费侧 failures 是不同状态，不能混用。

## 第三步：接受重复，复用 Inbox

| 故障位置 | 恢复时数据库状态 | 恢复动作 |
| --- | --- | --- |
| 业务事务回滚 | 无课程、无任务 | 不投递 |
| 业务已提交，尚未 XADD | 任务 pending | 重放相同事件 |
| XADD 失败且没有写入 | 任务 pending，可能记录失败 | 修复后重试 |
| XADD 成功但响应丢失 | 任务仍 pending | 可能再生成一条消息 |
| XADD 返回成功，数据库标记失败 | 任务仍 pending | 再发同一事件 |
| 标记完成 | 任务不再被扫描 | 等待消费者独立处理 |

最后两种不确定状态会形成不同 Stream ID、相同 eventId 的消息。消费者组内 Inbox 以 eventId 去重，将通知写入和去重记录放在同一事务，然后分别 ACK 两次投递；结果是一行数据库通知。

这不是 Redis 消息去重，也不是外部邮件只发送一次，更不是数据库与 Redis 的分布式事务。

## 验证观察点

默认测试使用真实 H2 与模拟队列，验证同事务提交、Outbox 失败回滚、业务失败不新增事件、错误数据源、非实际事务、嵌套事务、载荷快照、目标隔离、批次限制、失败记账和异常传播。文件库关闭重开后，待发布事件仍能恢复。

五项 Redis 集成测试验证：

1. 课程提交到 Stream，再到 Inbox 的完整链路。
2. 真实 Redis 键类型错误使投递失败，修复后可重试。
3. 真正 XADD 后模拟响应丢失，重投生成两条消息但只有一个数据库效果。
4. 真正 XADD 后模拟数据库标记失败，同事件重投仍被 Inbox 去重。
5. Outbox 插入后业务事务回滚，投递器没有可发送任务。

响应丢失和数据库标记异常是受控故障注入，不是操作系统进程崩溃或 Redis 掉电测试。

## 生产边界

每个目标只运行一个本练习投递器。synchronized 只锁住当前 Java 对象，不协调多个实例；并发实例可能重复发送或条件更新冲突。没有分布式抢占、租约、后台调度、退避、发布重试上限、自动归档或顺序保证。

Redis 返回成功后仍可能因持久化、故障切换或消息保留策略丢失记录；Outbox 已发布标记不会自动回退。需要额外设计持久化、高可用、容量、对账和人工恢复。不能把本地开发的无持久化 Redis 直接当成生产可靠消息服务。

Outbox 历史及 Inbox 去重记录需按消息重放窗口保留，删除前评估重放与审计要求。本例 H2 用于教学，迁移外部数据库时需重新验证 SQL、隔离级别和故障恢复。

## 自检与下一步

- 为什么事务提交后再发送仍需 Outbox？
- 为什么重试必须保留 eventId 和载荷快照？
- published 与消费完成、死信分别是什么关系？
- 多个投递器并发时，仅靠 synchronized 为什么不够？

继续按照[学习路线](../../roadmap/index.md)进入搜索基础，再学习 RPC 与中间件综合实践。

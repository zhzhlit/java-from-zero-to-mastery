---
title: 毒消息、重试限制与死信处理
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 毒消息、重试限制与死信处理

## 学习目标

- 将永久失败、暂时性业务失败和基础设施故障分开处理。
- 限制失败次数，将无法继续处理的消息保留到死信 Stream。
- 先保存死信再确认原消息，保护正常的终态重放。
- 理解 Lua 串行执行、数据库事务和端到端一致性的不同边界。

## 前置知识与练习入口

先完成 [消费者失败恢复与消息幂等](./06-consumer-recovery-inbox.md)。本章扩展原模块，不增加依赖。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/starter)：补齐 StreamFailurePolicy 三个入口及 ResilientNotificationConsumer.processBatch；Lua 脚本、结果类型和辅助方法已提供。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/solution)：完整实现与默认、真实 Redis 测试。

```bash
mvn -B -pl exercises/message-queue-basics/solution -am test
mvn -B -pl exercises/message-queue-basics/solution -am verify -Predis-it -Dredis.port=16389
```

本章新增 12 项默认测试、8 项 Redis 集成测试；加入发布 Outbox 后模块共 46 项默认测试、25 项集成测试。starter 测试类均禁用，按任务完成后移除对应 `@Disabled`。专用 Redis 启动、依赖复制和 DeadLetterDemo 运行见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics#readme)。不要连接生产 Redis。

## 失败分类

| 情况 | 策略 | 原因 |
| --- | --- | --- |
| 事件字段非法或版本不支持 | INVALID_EVENT，立即死信 | 重读不会修复原字段 |
| 同组同事件 ID 对应不同载荷 | EVENT_ID_CONFLICT，立即死信 | 不能静默视为同一事件 |
| 数据库约束失败 | BUSINESS_CONSTRAINT，立即死信 | 本示例不自动修改业务数据 |
| Spring TransientDataAccessException | TRANSIENT_DATABASE，计数后重试 | 仅明确识别的暂时性数据库错误 |
| 未知错误、Redis 不可用或配置错误 | 抛出原错误 | 不把基础设施故障伪装成毒消息 |

分类是本练习的业务约定，不是所有系统的通用规则。配置上限为 3 时，第一次失败记为 1，第三次失败进入死信。不是“失败一次后再重试三次”。失败计数按来源 Stream、消费组和消息 Stream ID 隔离，不使用事件 ID 替代投递 ID。

批次逐条处理，可识别的坏消息留下失败结果后继续下一条；未知故障直接中断，已经提交的前缀不会回滚。批次结果携带原异常、来源 ID 与策略决策，调用方应记录并告警。

## 三个显式状态入口

```text
BEGIN -> 校验 pending 归属 / 已死信终态
  -> 解码事件 -> Inbox 事务提交
    -> SUCCESS -> XACK -> 清理失败计数
  -> 可识别业务失败
    -> FAILURE -> 增加次数
      -> 未到上限：保留 pending
      -> 达到上限：追加死信 -> 保存终态 -> XACK
```

BEGIN 在执行数据库业务前检查终态，避免同一缓冲批次重复执行已死信消息。旧消费者在消息被接管后尝试改变 Redis 状态会得到 OWNERSHIP_LOST；但检查不能取消已经开始的数据库操作，不是跨系统 fencing。

未到上限并不意味着自动调度。本章仍需显式调用 readOwnPending，或按上一章接管空闲消息；没有后台循环、指数退避或定时队列。

## 数据库已成功，不再计入业务失败

Inbox 将去重记录与通知写入同一个事务。事务成功返回后，消费者才调用 SUCCESS；确认异常不放在业务失败分类的 catch 中。

若提交后 Redis 连接断开，原 pending 仍可恢复，Inbox 将重放识别为重复，再次确认。不能因为 Redis ACK 失败就增加业务失败次数或把已完成业务的消息当成毒消息。测试通过提交后关闭客户端制造这个间隙，而不是只模拟返回值。

## 死信是审计记录，不是丢弃消息

死信键为 `sourceStream:dead:group`，失败状态键为 `sourceStream:retry:group`。死信包含 dlqVersion、sourceStream、sourceGroup、sourceId、errorCode、failures、payloadMissing，以及以 `payload.` 为前缀的原始字段。

原消息已被 XDEL 删除但仍存在 pending 时，死信保存来源 ID 和 `payloadMissing=1`；不会编造载荷。原消息 ACK 后仍可能保留在来源 Stream，ACK 不等于 XDEL。

成功追加死信并保存终态后，相同投递的正常重放返回 ALREADY_DEAD_LETTERED，不重复追加。终态没有自动过期；需要按重放周期制定保留和清理策略。死信可能包含敏感原文，必须限制访问、审计并控制容量，不能直接展示给普通用户。

## Lua 能保证什么

本例利用 Lua 在一次脚本内检查归属、类型与计数，再执行死信和确认，避免其他客户端在脚本命令间插入。常见键类型错误在写入前失败，原消息保持 pending。

脚本运行错误不会回滚已经执行的写入。例如 XADD 成功后 HSET 因资源问题失败，可能只留下死信而未保存终态，重放仍可能产生重复死信。不能把“脚本串行执行”解释为数据库式回滚或 exactly-once。

本例面向单实例 Redis，三个键没有为 Cluster 同槽设计。也不保证 Redis 重启后数据仍在、数据库与 Redis 同时提交或外部邮件只发一次。生产需要独立设计持久化、容量、可靠发送、人工处理和恢复审计。

## 验证观察点

默认测试检查参数、分类、批次继续处理、原异常保留、成功与重复路径，以及确认错误不计数。真实 Redis 测试验证：

1. 坏消息进入死信，同批健康消息成功。
2. 暂时失败到限，终态重放不新增死信或再次执行业务。
3. 键类型错误不确认原消息，修复后可继续。
4. 接管后的旧消费者不能更改消息状态。
5. 提交后确认失败通过 Inbox 恢复，不计入毒消息。
6. 重试成功清理计数，消费组状态隔离。
7. 原载荷删除后仍留下来源审计信息。

## 自检

- 为什么“读过几次”不直接等于“业务失败几次”？
- 为什么只捕获明确的业务异常，而不 catch 所有 RuntimeException 后死信？
- 死信追加成功、终态保存失败时，你会如何发现和处置重复？
- 什么条件下可以安全清理终态与 Inbox 记录？

## 下一步

下一章：[可靠发布与 Outbox 衔接](./08-outbox-stream-publishing.md)。

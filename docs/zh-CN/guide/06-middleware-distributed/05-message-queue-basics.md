---
title: 消息队列基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 消息队列基础

## 学习目标

- 理解生产者、消息、消费组、消费者与确认的职责。
- 使用 Redis Streams 验证新消息投递和自己的未确认消息重读。
- 区分同组负载分摊与不同组各自接收。
- 识别消息已读、业务已处理和消息已确认三个状态。

## 前置知识

已完成 [可靠失效与失败恢复](./04-cache-invalidation-outbox.md)，理解失败可恢复动作与重复执行。本章新增独立 `message-queue-basics` 模块，使用现有 BOM 管理的 Jedis 5.0.2，不与缓存练习的业务对象混在一起。

这里选择 Redis Streams 演示消息队列基础，不代表它可直接替代所有 RabbitMQ 或 Kafka 场景。核心存储和消费组使用真实 Redis，不手写 broker。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/starter)：补全 CourseEventQueue 的公开操作，字段模型和读取辅助方法已提供。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/solution)：完整实现、10 项默认测试、8 项真实 Redis 集成测试。

```bash
mvn -B -pl exercises/message-queue-basics/solution -am test
mvn -B -pl exercises/message-queue-basics/solution -am verify -Predis-it -Dredis.port=16389
```

starter 两个测试类初始禁用，完成后移除标记。专用 Redis 和示例启动命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics#readme)。

本章基础测试仍为 10 项默认、8 项真实 Redis；模块加入下一章的恢复和 Inbox 扩展后，总计为 22 项默认、12 项 Redis 集成测试，starter 对应四个测试类分别禁用。

## 消息为什么不同于直接调用

课程发布后可以产生一个事件，消费者稍后处理。生产者和消费者不必在同一调用栈里，但也增加了投递状态、失败、重复和积压等问题。

本例事件包含 schemaVersion、eventId、courseId 和 title。Jedis 使用 Redis 的字段映射传输，不手拼 JSON。只接受版本 1，校验 UUID、正数课程 ID 和标题边界，避免把任意字段当成有效业务事件。

eventId 是业务事件标识，Redis Stream ID 是本次追加记录的位置。同一业务事件若被发布两次，可能对应两个 Stream ID；不能用“记录 ID 不重复”推导“业务不会重复”。

## 生产与消费组初始化

XADD 追加消息，由 Redis 生成 Stream ID。消费组初始化使用 `0-0` 并允许创建空 Stream，因此可读取建组前的积压消息；若从 `$` 创建，则起点语义不同，不能随意互换。

重复初始化仅忽略明确的 BUSYGROUP 错误，不重置投递位置；权限错误、连接失败等继续传播。不能用捕获所有异常的方式假装消费组已就绪。参考 [XGROUP CREATE](https://redis.io/docs/latest/commands/xgroup-create/)。

## 新消息与 pending

消费使用 XREADGROUP：

- `>`：获取本消费组尚未投递的新消息。
- `0-0`：重读指定消费者自己已投递但尚未确认的消息。

本例读取不使用 NOACK，也不自动调用 XACK。消息读到后进入该组的 pending 列表，单纯返回 Java 对象并不表示处理已完成。

本项目的 Jedis 5.0.2 在解析空 XPENDING 摘要时存在空值处理问题，因此 pendingCount 使用 [XINFO GROUPS](https://redis.io/docs/latest/commands/xinfo-groups/) 的组级计数；组不存在仍报错，不将错误当成 0。Jedis 的 MINIMUM_ID 是范围端点 `-`，本章建组和历史读取明确使用 `new StreamEntryID("0-0")`，不要误用该常量。

readOwnPending 不会替另一个消费者取回任务。更换消费者名称后，不能认为原消费者的 pending 自动归自己；接管需要后续单独设计。命令语义见 [XREADGROUP](https://redis.io/docs/latest/commands/xreadgroup/)。

本例每次读取最多 1 到 100 条，不做阻塞轮询。没有消息时返回空列表；连接或服务错误不转换成“没有消息”。

## 同组分摊与不同组各自接收

同一个消费组里，新消息在消费者间分配。本例依次让 worker-1、worker-2 各取一条，得到不同 ID；不会把每条新消息都自动广播给组内所有消费者，也不保证公平调度。

不同消费组各有投递位置和 pending 状态。本例 notifications 和 analytics 都能收到同一条事件；notifications 的确认不会清除 analytics 的 pending。

消费组名称代表处理用途，不应为了每次进程重启随机生成新组。消费者名称也要有明确生命周期，尤其是未确认消息尚未处理完时。

## 处理完成后再确认

示例流程为读取、校验并输出课程 ID，再显式 acknowledge。业务处理或解析失败时不确认，消息仍处于 pending，后续可重读。XACK 返回确认数量，本例将 1 视为成功、0 视为该 ID 已不在该组 pending。

XACK 不删除 Stream 中的记录。真实测试在确认后检查 pending 变为 0，但 XLEN 仍为 1，参考 [XACK](https://redis.io/docs/latest/commands/xack/)。因此确认机制也不是保留策略，不能无限保留所有历史消息而不做容量设计。

如果业务副作用已经完成，确认前进程退出，消息之后仍可能再次执行；若先确认再做业务，失败时反而可能丢掉处理机会。本章不实现业务去重，也不声称 exactly-once。

## 坏消息与空载荷

不支持的版本、缺失字段或格式错误会导致解码失败，不自动跳过或确认。原始 QueuedMessage 保留 Stream ID，便于后续诊断与恢复。已被删除的 pending 记录可能只剩 ID，本例将其作为缺失载荷拒绝，而不是伪造一个正常事件。

持续重读同一个坏消息可能阻塞恢复流程；生产需要毒消息分类、重试限制、接管和死信等策略。本章只保留失败状态，不用自动确认掩盖问题。

## 验证证据与存储边界

10 项默认测试验证字段映射、错误传播、读取参数、不自动确认、不可变载荷及输入边界；它们不证明 Redis 行为。

8 项真实集成测试覆盖建组前积压消息、同组分摊、消费者对象重建后的自身 pending、不同组独立状态、坏消息保留、批次上限与重复初始化不重置位置和已删除 pending 的空载荷。测试只删除自己的随机 Stream，不清空整个数据库。

本地教学服务关闭持久化，消费者对象重建测试也不是操作系统进程崩溃验证。生产还需设计 Redis 持久化、高可用和保留策略。消息实例使用 noeviction；不要与会任意淘汰键的缓存共用资源，否则 Stream 也可能丢失。

## 分级练习

- **基础**：完成 XADD 和消费组初始化，区分两个 ID。
- **进阶**：完成新消息、自己 pending 的读取与显式确认。
- **挑战**：跑真实服务测试，解释同组、不同组以及坏消息的最终 pending 状态。

## 常见错误与排查

- 把读取当成完成：检查 pending，而不只看是否拿到 Java 对象。
- pending 看不到：确认组与消费者名称，以及是否误用了 `>`。
- 每个组都收到消息被误认为重复分配：不同组本来就维护独立投递状态。
- 确认后内存不下降：XACK 不负责删除或裁剪历史条目。
- 坏消息被悄悄丢掉：检查是否在异常处理或 finally 中无条件确认。
- 连接失败变成空批次：不要吞掉 Redis 异常，空列表只代表成功读取后没有数据。

## 面试与复习题

1. eventId 与 Stream ID 各自表示什么？
2. 同组分摊与不同组各自接收有什么区别？
3. `>` 和 `0-0` 分别读取哪一类消息？
4. XACK 后为什么 Stream 长度还可能不变？
5. 为什么处理完成后再确认仍不能保证副作用只发生一次？
6. 为什么自己的 pending 读取不能替代跨消费者接管？

## 本章总结

消息队列引入独立的投递与确认状态。先用真实服务观察这些状态，再讨论消费者失败恢复、业务幂等与可靠发送。

## 下一步

上一章：[可靠失效与失败恢复](./04-cache-invalidation-outbox.md)

下一章：[消费者失败恢复与消息幂等](./06-consumer-recovery-inbox.md)，验证跨消费者接管和确认前后的数据库状态。

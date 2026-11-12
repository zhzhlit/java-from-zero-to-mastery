---
title: 消费者失败恢复与消息幂等
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 消费者失败恢复与消息幂等

## 学习目标

- 显式接管其他消费者的空闲 pending，并保留扫描游标。
- 将业务副作用和消费去重记录放入同一个数据库事务。
- 数据库提交后再确认，重放时避免重复数据库副作用。
- 区分数据库幂等、消息确认和端到端 exactly-once。

## 前置知识

已完成 [消息队列基础](./05-message-queue-basics.md)和 [事务提交后缓存失效](./03-cache-commit-invalidation.md)。本章扩展已有消息模块，使用现有基线中的 Spring JDBC/H2，不增加新的中间件。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/starter)：补齐 claimIdle、NotificationInbox、NotificationConsumer。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics/solution)：新增 12 项默认测试和 4 项真实 Redis 测试。

```bash
mvn -B -pl exercises/message-queue-basics/solution -am test
mvn -B -pl exercises/message-queue-basics/solution -am verify -Predis-it -Dredis.port=16389
```

加上上一章，模块共 22 项默认测试、12 项集成测试。starter 对应测试类禁用，完成后移除标记；运行专用 Redis 和恢复示例见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/message-queue-basics#readme)。

## 自己的 pending 不等于接管

上一章 readOwnPending 只能读取指定消费者自己的待确认记录。原消费者停止后，新消费者需要显式接管，不能换一个名称再读取 `0-0` 就期待得到旧任务。

本章使用成熟服务的 XAUTOCLAIM，输入目标消费者、最小空闲时间、起始游标和批次上限。结果包含下一扫描位置与本次接管的消息，参考 [官方 XAUTOCLAIM 文档](https://redis.io/docs/latest/commands/xautoclaim/)。

ClaimBatch 保留 nextStart。一次没有找到符合阈值的消息，不等于已经扫描完所有 pending；调用方应使用返回游标继续后续扫描，返回 `0-0` 才表示这轮扫描完成。本例不在库里无限循环扫描。

测试使用 Duration.ZERO 立即接管，避免依赖 sleep。生产必须根据正常处理耗时选择正阈值；否则旧消费者可能仍在工作，两个消费者就可能同时处理同一事件。接管是投递所有权变化，不是对业务执行的全局锁。

Jedis 5.0.2 的该接口返回游标和消息，不单独暴露新版本 Redis 响应中的已删除 ID 列表。本例不通过裁剪或删除 pending 来实现恢复；生产还要独立考虑载荷保留和清理监控。

## Inbox 与数据库副作用

本章的业务效果是一条 course_notifications 通知行，不发送邮件或请求外部服务。消费去重表 inbox_receipts 以 `(consumer_group, event_id)` 为主键，保存原始课程编号和标题以核对同 ID 的内容。

一次处理在同一数据源事务中执行：

1. 插入 Inbox 去重记录。
2. 插入业务通知行。
3. 事务提交。
4. 成功提交后才 XACK。

若通知写入失败，去重记录也回滚，后续能重新处理。不同消费组有独立去重空间；Queue 与 Inbox 的组必须一致，不能随意复用其他用途的去重记录。

JdbcTemplate、事务管理器必须使用同一 DataSource。本例拒绝在已有外层事务中处理，避免方法先返回并确认，而外层之后又回滚。还检查模板确实建立了实际事务，不把无事务执行当成原子处理。事务生命周期参考 [Spring TransactionTemplate API](https://docs.spring.io/spring-framework/docs/6.1.16/javadoc-api/org/springframework/transaction/support/TransactionTemplate.html)。

## 重复键必须在失败事务外处理

不能仅靠“先查有没有，再插入”保证并发安全；数据库唯一约束才是最后边界。本例先尝试事务写入，如果重复键失败，等整个失败事务回滚完成后，再从已提交的 Inbox 读取原事件。

- 找到相同组、同 ID、相同载荷：这是已处理事件，跳过重复业务写入。
- 同 ID 却有不同载荷：明确报错，不掩盖生产者的标识冲突。
- 没有对应的已提交去重记录：继续抛出原始重复键错误，不把其他约束问题都当成正常重复。

这种检查不能在已失败的事务里继续执行；有些数据库会拒绝之后的语句。并发测试使用两个线程和真实 H2 唯一约束，验证同组同事件仅产生一条通知。

## 数据已提交，确认却失败

最重要的恢复间隙发生在数据库提交之后、XACK 成功之前。此时重复投递仍可能发生，但业务数据已经存在。

本章真实测试先写入通知，关闭原确认客户端让 XACK 失败，再由新客户端接管并处理：

- Inbox 识别相同事件，重复数据库写入被跳过。
- 新消费者确认当前 Stream ID。
- 通知总数仍为 1，pending 变为 0。

如果先 ACK 再写数据库，数据库失败时就可能失去处理机会。因此确认必须排在提交之后；确认异常也不能说明数据库没有发生改变。

## 批次与失败边界

NotificationConsumer 接受来自同一队列和消费组的原始批次，逐条解码、提交、确认，最多 100 条。解析或数据库错误不会自动确认；确认失败会传播异常。

批次不是一个大事务。处理到中间失败时，之前的消息可能已经提交并确认，当前消息也可能已提交但尚未确认。之后恢复或重读时依靠 Inbox，而不是期待整批回滚。

坏消息和已删除载荷仍需要后续毒消息或死信策略，不能靠无限接管解决。本例不吞异常，也不为了清空 pending 自动确认失败消息。

## 验证证据

新增 12 项默认测试使用真实 H2 与模拟 Redis，验证接管参数/游标、输入边界、不同 Stream ID 的同事件去重、业务失败回滚、确认失败重放、坏消息、组隔离、拒绝外层事务、载荷冲突、并发重复、文件库关闭重开及批次边界。

新增 4 项真实 Redis 测试验证所有权接管、提交后确认故障恢复、未达到空闲阈值时不接管以及坏消息接管后仍保留 pending。测试对象重建和连接故障不是完整进程崩溃或断电验证。

文件型 H2 测试在关闭数据库后用新连接和新 Inbox 读取原记录，没有重新写去重状态。它证明去重不依赖旧 Java 对象，但不代表已完成生产数据库灾备验证。

## 幂等能力的边界

本例保证的是同一数据库事务中的通知行不会因相同组/eventId 重放而重复插入，不是端到端 exactly-once。外部邮件、支付或 HTTP 请求不能靠这两张本地表自动回滚。

生产需要独立设计外部幂等接口、可靠发送、数据库与 Redis 的持久化、重试和监控。去重记录保留周期必须覆盖消息重放周期；提前删除记录后再重放旧消息，仍可能重复产生业务效果。

## 分级练习

- **基础**：完成接管参数校验，保留服务返回的扫描游标。
- **进阶**：完成 Inbox 与通知的同事务写入，在失败事务之外处理重复键。
- **挑战**：完成提交后确认，验证确认故障、并发重复和载荷冲突。

## 常见错误与排查

- 新消费者读不到旧 pending：区分自身重读与跨消费者接管。
- 空批次就停止全部扫描：检查 nextStart，不能忽略游标。
- 通知失败后重放被跳过：检查去重记录是否与业务操作共同回滚。
- 外层事务未提交就确认：保持本例的独立事务边界，不吞掉事务错误。
- 同 ID 内容变了却被跳过：应当检查已记录载荷并报错。
- 认为有 Inbox 就不会重复发送邮件：本章只覆盖数据库副作用，不覆盖外部服务。

## 面试与复习题

1. XAUTOCLAIM 与自己的 pending 重读有什么区别？
2. 为什么接管空闲时间不能随意设为 0？
3. 去重记录与业务写入为什么必须在同一事务中？
4. 重复键错误为什么要在失败事务之外检查？
5. 数据已提交但 XACK 失败时，重放会发生什么？
6. 本例为什么不能称作端到端 exactly-once？

## 本章总结

恢复机制允许消息再次执行，Inbox 约束数据库副作用，确认则结束本消费组的待处理状态。三者要分别设计和验证，而不是通过提前确认或吞掉异常消除表面的失败。

## 下一步

上一章：[消息队列基础](./05-message-queue-basics.md)

继续按照[学习路线](../../roadmap/index.md)进入毒消息、重试限制与死信处理。

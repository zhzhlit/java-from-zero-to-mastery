# 消息队列、可靠发布与消费者恢复练习

使用 Redis Streams 与现有 Jedis 5.0.2，学习生产、消费组、pending 和显式确认。本地验证 Redis 7.4.12，不手写消息存储或消费组引擎。

## 任务

- 基础：完成 XADD 与消费组初始化，使用版本化字段映射表示课程发布事件。
- 进阶：区分 `>` 读取新消息、`0-0` 读取自己的 pending，读取不自动确认。
- 挑战：处理完成后显式 XACK，验证同组分摊、不同组各自接收与未确认重读。

starter 提供队列、空闲接管、Inbox、失败策略、发布 Outbox 和消费者的可编译骨架，字段编解码、Lua 脚本和读取辅助方法已提供。46 项默认测试、25 项真实 Redis 测试在九个测试类上禁用。完成后移除对应测试类的 `@Disabled`。solution 完整实现并启用全部对应测试。

## 默认验证

```bash
mvn -B -pl exercises/message-queue-basics/starter -am test
mvn -B -pl exercises/message-queue-basics/solution -am test
```

solution 运行 10 项基础测试、12 项恢复/Inbox 测试、12 项失败策略测试和 12 项发布 Outbox 测试，starter 跳过 46 项。数据库测试使用真实 H2 和模拟 Redis；默认测试不证明 Redis 服务行为。

## 真实 Redis 验证

使用专用本地实例，不连接生产环境。Docker 可用时：

```bash
docker run --rm -d --name java-mastery-streams -p 127.0.0.1:16389:6379 redis:7.4-alpine redis-server --save '' --appendonly no --maxmemory 32mb --maxmemory-policy noeviction
mvn -B -pl exercises/message-queue-basics/solution -am verify -Predis-it -Dredis.port=16389
docker stop java-mastery-streams
```

本机安装 Redis 时可在另一个终端运行：

```bash
redis-server --bind 127.0.0.1 --protected-mode yes --port 16389 --save '' --appendonly no --maxmemory 32mb --maxmemory-policy noeviction
```

预期 solution 通过 46 项默认测试、25 项集成测试。服务不可达时 profile 失败，不自动跳过。每项测试使用随机 Stream，仅删除自身键；现有 CI 的 Redis 服务和 `redis-it` profile 会包含本模块。

消息实例使用 noeviction，避免在内存压力下把 Stream 当作可淘汰缓存删除；达到上限时写入可能失败，需要调用方处理。开发命令关闭持久化，仅验证运行期行为，不证明 Redis 重启后消息还在。生产需独立设计持久化、高可用、保留策略和容量管理，不与可任意淘汰的缓存混用。

## 运行示例

专用 Redis 启动后，在仓库根目录按顺序执行：

```bash
mvn -B -pl exercises/message-queue-basics/solution -am compile
mvn -B -f exercises/message-queue-basics/solution/pom.xml dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
REDIS_PORT=16389 java -cp 'exercises/message-queue-basics/solution/target/classes:exercises/message-queue-basics/solution/target/dependency/*' io.github.javamastery.exercises.messaging.MessageQueueDemo
```

Windows 请调整环境变量语法和 classpath 分隔符。关键输出：

```text
New messages: 1
Pending before ack: 1
Pending replay count: 1
Processed course: 1
Pending after ack: 0
```

示例处理动作为校验事件并输出课程编号，不发送真实通知；最后删除自己的随机 Stream 并关闭客户端。

## 消费者恢复与 Inbox

`claimIdle` 使用 XAUTOCLAIM，将满足空闲阈值的 pending 交给新消费者，并返回 `ClaimBatch.nextStart` 扫描游标。空批次不一定意味着扫描结束，不能丢弃返回游标。示例和测试使用 0 空闲时间获得确定结果；生产应根据处理耗时选择正阈值，避免接管仍在工作的消费者。

`NotificationInbox` 将消费组内的事件去重记录与通知行写入同一个 JDBC 事务，数据库提交后 `NotificationConsumer` 才 XACK。相同 group/eventId 重放不重复插入通知；相同 ID 携带不同载荷会报错。这里的副作用仅是数据库通知行，不发送真实邮件或调用外部接口。

Inbox 必须使用自己的实际事务，不允许被外层事务包裹后提前确认；JdbcTemplate 和事务管理器使用同一 DataSource。重复键在失败事务回滚后检查已提交状态，不能在已出错的事务内继续查询。

专用 Redis 已启动并完成上述依赖复制后：

```bash
REDIS_PORT=16389 java -cp 'exercises/message-queue-basics/solution/target/classes:exercises/message-queue-basics/solution/target/dependency/*' io.github.javamastery.exercises.messaging.ConsumerRecoveryDemo
```

关键输出：

```text
Recovered messages: 1
New effects during replay: 0
Duplicates during replay: 1
Stored notifications: 1
Pending after recovery: 0
```

示例显式模拟数据库提交后未确认的间隙，再接管并重放；不是操作系统进程崩溃测试。默认测试另用文件型 H2 关闭重开，验证去重记录不依赖旧 Java 对象。

## 重试与死信

`StreamFailurePolicy` 使用 Lua 检查 pending 归属并维护消费组内失败次数；`ResilientNotificationConsumer` 逐条分类失败，坏消息不阻塞同批健康消息。上限包含第一次失败：配置 3 表示第三次失败进入死信，而非额外重试三次。只有 `TransientDataAccessException` 计入可重试失败；非法事件、同事件 ID 载荷冲突、业务约束失败立即进入死信。未知错误直接抛出，不擅自分类。

数据库提交后确认失败不增加业务失败计数，后续依靠 Inbox 去重再确认。失败信息和原异常保留在批次结果中，调用方负责记录、告警和调度，不吞掉错误。

脚本先追加死信，再保存终态和确认原消息。死信保留来源 Stream、消费组、Stream ID、错误码、失败次数与 `payload.` 前缀的原始字段；原载荷已删除时保留元数据及 `payloadMissing=1`。终态标记使正常响应重放不重复追加死信，也不重新执行业务。成功重试清理失败计数。

完成依赖复制并启动专用 Redis 后：

```bash
REDIS_PORT=16389 java -cp 'exercises/message-queue-basics/solution/target/classes:exercises/message-queue-basics/solution/target/dependency/*' io.github.javamastery.exercises.messaging.DeadLetterDemo
```

```text
Applied notifications: 1
Dead-lettered messages: 1
Dead-letter stream length: 1
Pending after processing: 0
```

新增 8 项真实 Redis 测试覆盖坏消息隔离、重试到限、终态重放、死信键类型错误、接管后的归属检查、提交后确认失败、成功清理、组隔离和载荷已删除的审计。

## 可靠发布与 Outbox

`CoursePublishingService` 在同一数据源的实际事务中插入课程和发布事件，不访问 Redis。`PublicationOutbox` 保存目标 Stream、稳定 eventId 与标题快照；重试只读取已有事件，不生成新 ID 或重读当前课程标题。事务中任一写入失败，两者一起回滚。服务拒绝外层事务；错误事务管理器在写入前被资源绑定检查拒绝。

`PublicationRelay.relayBatch` 必须在业务事务外显式调用：按目标选择有界待发布批次，XADD 成功后保存返回的 Stream ID，只有这时任务才标记已发布。Jedis 异常保留任务、记录次数并返回原异常；未知异常、数据库确认失败直接抛出。记录失败次数自身出错时保留原 Redis 异常为 suppressed exception。批次结果的 published 不代表通知已经完成。

专用 Redis 已启动并完成依赖复制后：

```bash
REDIS_PORT=16389 java -cp 'exercises/message-queue-basics/solution/target/classes:exercises/message-queue-basics/solution/target/dependency/*' io.github.javamastery.exercises.messaging.OutboxPublicationDemo
```

```text
Pending before relay: 1
Published messages: 1
Applied notifications: 1
Pending outbox: 0
Pending messages: 0
```

真实 Redis 测试覆盖完整链路、实际命令失败恢复、真实 XADD 后模拟响应丢失、发送后数据库确认失败及事务回滚。两种发送不确定性会形成不同 Stream ID、相同 eventId 的重复消息，Inbox 只产生一行通知。默认测试另以文件型 H2 关闭重开验证待发布事件的持久性，不把内存集合当 Outbox。

## 边界

读取、接管和发布批次为 1 到 100，非阻塞；模块没有后台调度、退避或死信重投。自己的 pending 不是所有消费者的消息，跨消费者恢复需显式接管。旧 `NotificationConsumer` 保持失败即抛出的教学行为；新策略由 `ResilientNotificationConsumer` 显式启用。

发布采用本地事务 Outbox，不是数据库与 Redis 的分布式事务。XADD 成功后响应丢失或数据库标记失败都会重发，属于允许重复的恢复方式。Redis 已返回成功但后来因持久化或保留配置丢失消息时，已发布任务不会自动修复。API 请求重试也不是 eventId 重放，重复发布相同课程 ID 会被数据库拒绝。

每个目标应只部署一个本练习投递器；synchronized 仅约束当前 Java 对象，没有跨进程抢占、租约或分布式锁。并发投递器可能重复发送并因条件更新冲突报错。按失败次数优先新任务不保证全局或同课程事件顺序；没有自动重试上限、清理或归档，生产需独立设计调度、监控、人工恢复与保留策略。

Lua 执行期间命令不会被其他客户端插入，但运行错误不会回滚已写状态。预检能避免常见键类型错误；若死信追加后保存终态时发生内存不足等错误，仍可能部分完成甚至重放出重复死信，不能宣称 exactly-once。消息、计数和死信键当前按单实例 Redis 设计，不直接支持 Cluster 跨槽执行。归属检查只保护 Redis 状态转换，不是数据库执行的全局 fencing。

失败 Hash 和死信不自动过期，应制定容量、终态保留及安全清理规则；提前清理可能失去重放保护。原始载荷可能包含敏感信息，需限制死信访问、审计并制定保留策略。本例不自动删源消息，也不把 ACK 当作删除消息。

Inbox 只约束相同数据源中的数据库副作用；不能保证外部邮件、HTTP 调用只执行一次，也不能弥补 Redis 未持久化时的消息丢失。去重记录的保留周期必须覆盖可重放消息周期。批次逐条提交，失败前的前缀可能已经完成，不是整个批次原子回滚，不宣称端到端 exactly-once。

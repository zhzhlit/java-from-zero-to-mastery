# 消息队列与消费者恢复练习

使用 Redis Streams 与现有 Jedis 5.0.2，学习生产、消费组、pending 和显式确认。本地验证 Redis 7.4.12，不手写消息存储或消费组引擎。

## 任务

- 基础：完成 XADD 与消费组初始化，使用版本化字段映射表示课程发布事件。
- 进阶：区分 `>` 读取新消息、`0-0` 读取自己的 pending，读取不自动确认。
- 挑战：处理完成后显式 XACK，验证同组分摊、不同组各自接收与未确认重读。

starter 提供队列、空闲接管、Inbox 和消费者的可编译骨架，字段编解码和读取辅助方法已提供。22 项默认测试、12 项真实 Redis 测试在四个测试类上禁用。完成后移除对应测试类的 `@Disabled`。solution 完整实现并启用全部对应测试。

## 默认验证

```bash
mvn -B -pl exercises/message-queue-basics/starter -am test
mvn -B -pl exercises/message-queue-basics/solution -am test
```

solution 运行 10 项基础测试和 12 项恢复/Inbox 测试，starter 跳过 22 项。Inbox 测试使用真实 H2 和模拟 Redis；默认测试不证明 Redis 服务行为。

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

预期 solution 通过 22 项默认测试、12 项集成测试。服务不可达时 profile 失败，不自动跳过。每项测试使用随机 Stream，仅删除自身键；现有 CI 的 Redis 服务和 `redis-it` profile 会包含本模块。

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

## 边界

读取和接管批次为 1 到 100，非阻塞；模块没有后台调度、死信、重试限制或事务性消息发送。自己的 pending 不是所有消费者的消息，跨消费者恢复需显式接管。解析失败仍保留 pending，不自动确认；死信与毒消息策略属于后续内容。

Inbox 只约束相同数据源中的数据库副作用；不能保证外部邮件、HTTP 调用只执行一次，也不能弥补 Redis 未持久化时的消息丢失。去重记录的保留周期必须覆盖可重放消息周期。批次逐条提交，失败前的前缀可能已经完成，不是整个批次原子回滚，不宣称端到端 exactly-once。

# 消息队列基础练习

使用 Redis Streams 与现有 Jedis 5.0.2，学习生产、消费组、pending 和显式确认。本地验证 Redis 7.4.12，不手写消息存储或消费组引擎。

## 任务

- 基础：完成 XADD 与消费组初始化，使用版本化字段映射表示课程发布事件。
- 进阶：区分 `>` 读取新消息、`0-0` 读取自己的 pending，读取不自动确认。
- 挑战：处理完成后显式 XACK，验证同组分摊、不同组各自接收与未确认重读。

starter 提供可编译的公开操作骨架、字段编解码和读取辅助方法，10 项默认测试、8 项真实 Redis 测试在类级禁用。完成后移除两个测试类的 `@Disabled`。solution 完整实现并启用全部对应测试。

## 默认验证

```bash
mvn -B -pl exercises/message-queue-basics/starter -am test
mvn -B -pl exercises/message-queue-basics/solution -am test
```

solution 运行 10 项默认测试，starter 跳过 10 项；这些 Mockito 与字段测试不证明 Redis 服务行为。

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

预期 solution 通过 10 项默认测试、8 项集成测试。服务不可达时 profile 失败，不自动跳过。每项测试使用随机 Stream，仅删除自身键；现有 CI 的 Redis 服务和 `redis-it` profile 会包含本模块。

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

## 边界

读取批次为 1 到 100，非阻塞；本章不提供后台轮询、跨消费者接管、死信、业务去重或事务性消息发送。自己的 pending 不是所有消费者的待处理消息。解析失败应保留 pending，不自动跳过或确认；生产需要后续重试、接管与毒消息策略。

业务 eventId 与 Redis Stream ID 不相同，重复投递不等于重复副作用已经解决。确认应放在业务处理完成后；处理后确认前失败仍可能重复执行，不宣称 exactly-once。

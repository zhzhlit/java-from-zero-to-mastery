# 中间件综合实践

将课程发布、Outbox、Redis Streams、Lucene 搜索和 gRPC 权威查询串成一个可恢复流程。不新增第三方依赖；starter/solution 都显式依赖前面消息、搜索、RPC 模块的 solution，将其作为已完成课程的基础设施，本章只练习组合逻辑。

## 任务与数据

- ReviewCourseCatalog.release：同一实际数据库事务插入课程、描述/级别和稳定发布事件；不访问 Redis。
- CoursePublicationWorkflow.processBatch：校验来源，按 ID 更新索引，commit、refresh，再写幂等通知并 ACK。
- CoursePublicationWorkflow.backfill：从权威数据库补齐索引，不把 Inbox 回执当成索引已存在的证据。

starter 已提供查询适配、资源生命周期、表结构和演示，三个方法为可编译骨架；两个测试类使用 `@Disabled`。solution 启用 9 项默认测试和 5 项真实 Redis 测试。生成代码、网络和索引引擎沿用前置模块，不复制其实现。

课程只允许首次发布，不能更新或删除。事件携带 ID/标题，描述和级别从同事务写入的权威数据读取，没有伪造默认级别。RPC 直接读权威数据库，不依赖搜索或 Redis 可用。

## 验证

```bash
mvn -B -pl exercises/middleware-basics-review/starter -am test
mvn -B -pl exercises/middleware-basics-review/solution -am verify
```

`-am` 会运行前置 solution 的测试；本模块自身分别跳过/执行 9 项默认测试。使用专用 Redis（不要连接生产），启动方法见[消息模块 README](../message-queue-basics/README.md)。

```bash
mvn -B -pl exercises/middleware-basics-review/starter,exercises/middleware-basics-review/solution -am verify -Predis-it -Dredis.port=16389
```

本模块 solution 额外执行 5 项 Redis 测试；服务不可用时失败，不自动跳过。每项测试使用随机 Stream，结束时仅删除自己的键。真实链路包含 H2、Redis、Lucene 及回环 HTTP/2；文件索引测试另验证有序关闭重开与新索引补齐，不冒充进程崩溃或掉电测试。

## 演示

专用 Redis 启动后，在根目录按顺序运行：

```bash
mvn -B -pl exercises/middleware-basics-review/solution -am package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
REDIS_PORT=16389 java -cp 'exercises/middleware-basics-review/solution/target/classes:exercises/middleware-basics-review/solution/target/dependency/*' io.github.javamastery.exercises.middleware.MiddlewareReviewDemo
```

Windows 请调整环境变量和 classpath 分隔符。关键输出：

```text
RPC before projection: Java 综合实践
Search before projection: 0
Search after projection: 1
New notifications on replay: 0
Pending messages: 0
Pending outbox: 0
```

演示使用独立随机内存数据库、内存索引和随机 Stream；RPC 明文仅绑定 127.0.0.1 随机端口。最后关闭自身资源，不发送真实邮件，不启动后台消费者。

## 失败窗口

| 失败位置 | 已可能完成的状态 | 恢复 |
| --- | --- | --- |
| 课程/详情/Outbox 写入失败 | 本地事务全部回滚 | 修复后重新发布 |
| Redis 投递失败 | 权威课程已提交，Outbox 仍待发布 | 重试同一任务 |
| 索引写入或提交失败 | 消息仍 pending | 修复后重读/接管，按 ID 重写 |
| 索引已提交，通知事务失败 | 搜索可能可见，通知回执回滚 | 重放索引并补写通知 |
| 索引及通知已成功，ACK 失败 | 两侧已产生效果 | 重放索引，Inbox 防止重复通知 |
| ACK 后索引目录丢失 | 数据库回执仍存在 | 停止消费，向新空索引执行 backfill，再恢复消费 |

失败直接抛出，不把 IOException 转为空搜索，也不自动 ACK 坏消息。批次逐条提交，前缀可能已完成。已有数据库回执不能跳过索引写入，因为数据库、Lucene 和 Redis 不在同一事务。

## 边界

这是受信任、单消费者/受控接管的发布流程，不提供旧消费者 fencing、自动调度、退避或死信策略组合；毒消息分类仍见前置课程。来源载荷冲突或缺失会停止批次，需显式处理，不能无限忽略。不要将不同 Stream 的消息手动拼成批次。

backfill 只按 ID 补写，不删除旧索引中的多余文档；恢复应使用新空目录并暂停消费。本例一次读取全部课程，适用于教学数据，未实现在线原子切换、分页扫描或更新/删除版本控制。不要把此发布一次的模型直接推广为乱序更新处理器。

没有跨系统原子提交、端到端 exactly-once、Redis 持久化保证、权限过滤或生产 TLS。RPC 明文仅限本地；数据库通知只代表一行记录，不保证外部邮件只发送一次。各类历史、回执和消息的保留周期仍需独立设计。

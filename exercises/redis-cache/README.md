# Redis 共享缓存与事务失效练习

使用 Jedis 5.0.2（由现有 Boot BOM 管理）连接 Redis 7.4 系列，观察独立客户端共享缓存。本地验证版本为 Redis 7.4.12；基础示例使用内存源，事务扩展使用真实 H2 数据库，但不冒充多 JVM 集群验证。

## 任务

- 基础：实现 GET 命中与未命中回源，缓存值仅保存公共课程标题。
- 进阶：用单条 `SET ... PX` 写入值与 TTL，成功写源后 DEL 对应键。
- 挑战：验证命名空间隔离、两个客户端共享失效及异常传播，理解非原子读写的限制。

starter 提供三个基础缓存操作、提交后回调和事务服务的可编译骨架。19 项默认测试、10 项 Redis 集成测试在四个测试类上分别禁用。完成相应练习后移除对应类的 `@Disabled`。solution 默认启用所有对应测试。

## 默认验证：不需要 Redis

```bash
mvn -B -pl exercises/redis-cache/starter -am test
mvn -B -pl exercises/redis-cache/solution -am test
```

solution 执行 10 项基础 Mockito 测试与 9 项真实 H2 + 模拟 Redis 的事务测试；starter 跳过 19 项。这些测试不证明真实 Redis 行为。

## 真实 Redis 验证

只使用专用本地实例。Docker 可用时：

```bash
docker run --rm -d --name java-mastery-redis -p 127.0.0.1:16389:6379 redis:7.4-alpine redis-server --save '' --appendonly no --maxmemory 16mb --maxmemory-policy allkeys-lru
mvn -B -pl exercises/redis-cache/solution -am verify -Predis-it -Dredis.port=16389
docker stop java-mastery-redis
```

也可启动本机 Redis 7.4：

```bash
redis-server --bind 127.0.0.1 --protected-mode yes --port 16389 --save '' --appendonly no --maxmemory 16mb --maxmemory-policy allkeys-lru
```

另一个终端运行上述 Maven 集成验证命令。profile 使用 Failsafe 执行 `*IT`，Redis 不可达时失败，不会自动跳过。默认主机为 127.0.0.1、端口为 6379，可通过 `-Dredis.host`、`-Dredis.port` 指定本地测试服务。

预期 solution 的 19 项默认测试、10 项真实 Redis 集成测试通过；starter 在启用 profile 时额外跳过 10 项。集成测试使用随机命名空间，只删除已知测试键，不使用 FLUSHDB/FLUSHALL。CI 提供独立 Redis 服务并执行该 profile。

## 运行示例

专用 Redis 运行时，在仓库根目录按顺序执行：

```bash
mvn -B -pl exercises/redis-cache/solution -am compile
mvn -B -f exercises/redis-cache/solution/pom.xml dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
REDIS_PORT=16389 java -cp 'exercises/redis-cache/solution/target/classes:exercises/redis-cache/solution/target/dependency/*' io.github.javamastery.exercises.rediscache.RedisCacheDemo
```

Windows 请调整环境变量语法及 classpath 分隔符。关键输出：

```text
Source queries across two clients: 1
Source queries after shared invalidation: 2
```

示例只清理自己的随机命名空间键，关闭自己创建的客户端。

## 事务提交后失效扩展

`JdbcCourseRepository` 使用 H2，`TransactionalCourseService` 使用 Spring TransactionTemplate，`AfterCommitInvalidator` 注册 afterCommit 回调。加入外层事务时，等最外层提交后才失效；回滚时不失效。活动事务内的 Catalog 查询直接读源，不读取或回填共享缓存。

数据库事务更新必须使用 `TransactionalCourseService.rename`，而不是基础示例中面向内存源的 `RedisCourseCatalog.rename`。提交后回调只做 Redis 删除，不在已提交的 JDBC 事务中继续写业务数据。

专用 Redis 启动且上述依赖复制命令完成后运行：

```bash
REDIS_PORT=16389 java -cp 'exercises/redis-cache/solution/target/classes:exercises/redis-cache/solution/target/dependency/*' io.github.javamastery.exercises.rediscache.TransactionCacheDemo
```

关键输出：

```text
Before outer commit, shared cache: Java Basics
After commit: Committed Title
After rollback: Committed Title
```

事务测试不加测试级 `@Transactional`，而是显式完成真实提交与回滚。失效连接故障测试会确认数据库已提交但 Redis 仍为旧值；异常不是数据库回滚的证据。

## 明确边界

基础示例是顺序 cache-aside；事务扩展修正提交时序与事务内回填，但仍不提供跨客户端同键加载合并、并发回填强一致性或失效失败补偿。Redis 错误直接传播；afterCommit 删除失败时数据库已经提交，旧缓存可能等待 TTL 才消失，不能宣称两者原子提交。

TTL 是单键有效期，不是 Redis 总内存上限。练习实例还配置 maxmemory 和淘汰策略；生产需单独设计持久化、认证、TLS、ACL、超时、资源配额与恢复流程。不要连接生产 Redis，也不要将无认证实例暴露公网。

# Redis 共享缓存练习

使用 Jedis 5.0.2（由现有 Boot BOM 管理）连接 Redis 7.4 系列，观察独立客户端共享缓存。本地验证版本为 Redis 7.4.12；数据源仍是内存模拟，不冒充数据库或跨 JVM 集群。

## 任务

- 基础：实现 GET 命中与未命中回源，缓存值仅保存公共课程标题。
- 进阶：用单条 `SET ... PX` 写入值与 TTL，成功写源后 DEL 对应键。
- 挑战：验证命名空间隔离、两个客户端共享失效及异常传播，理解非原子读写的限制。

starter 的三个缓存操作是可编译骨架，10 项单元测试和 6 项集成测试各在类级 `@Disabled` 下禁用。完成后移除两个测试类的禁用标记。solution 完整实现并默认启用对应测试。

## 默认验证：不需要 Redis

```bash
mvn -B -pl exercises/redis-cache/starter -am test
mvn -B -pl exercises/redis-cache/solution -am test
```

solution 执行 10 项 Mockito 单元测试，只证明代码调用、错误与写入顺序，不证明 Redis 行为。starter 跳过 10 项。不要把它们当成真实服务验证。

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

预期 solution 的 10 项单元测试、6 项真实集成测试通过；starter 在启用 profile 时额外跳过 6 项。集成测试使用随机命名空间，只删除已知测试键，不使用 FLUSHDB/FLUSHALL。CI 提供独立 Redis 服务并执行该 profile。

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

## 明确边界

这是顺序 cache-aside 示例，不提供跨客户端同键加载合并，不解决并发回填旧值、数据库提交时序或 Redis 失效失败补偿。Redis 错误直接传播，不静默回源；更新源后删除 Redis 失败，源可能已经更新，调用方仍收到异常，旧缓存可能等到 TTL 才消失。

TTL 是单键有效期，不是 Redis 总内存上限。练习实例还配置 maxmemory 和淘汰策略；生产需单独设计持久化、认证、TLS、ACL、超时、资源配额与恢复流程。不要连接生产 Redis，也不要将无认证实例暴露公网。

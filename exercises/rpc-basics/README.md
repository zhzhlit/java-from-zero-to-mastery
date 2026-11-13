# RPC 基础练习

使用 gRPC Java 1.76.0、Protobuf/protoc 3.25.8，在 Java 21 上实现只读课程查询。协议、消息和 stub 由官方工具生成，不手写网络帧、序列化器或 RPC 调度引擎。固定版本用于可复现教学，不表示最新生产版本。

## 模块与任务

- contract：唯一的 `.proto` 来源，生成请求、响应、服务基类和客户端 stub。
- starter：完成 CourseRpcService.getCourse 与 CourseRpcClient.getCourse；目录、模型、构建和演示已提供。
- solution：完整实现以及 16 项默认测试。

starter 的两个测试类均使用 `@Disabled`，完成后移除。contract 是两侧共用的基础设施，不需要复制或修改生成文件。生成代码位于 target，不提交到 Git。首次构建会按操作系统下载 protoc 和 gRPC 生成器。

## 验证

```bash
mvn -B -pl exercises/rpc-basics/starter -am test
mvn -B -pl exercises/rpc-basics/solution -am verify
```

预期 starter 编译并跳过 16 项测试，solution 通过 16 项。13 项测试覆盖生成消息二进制往返、目录快照、输入边界、成功查询、服务端校验、NOT_FOUND、INTERNAL、deadline、每次调用的新预算、UNAVAILABLE、响应检查及不重试应用错误；另 3 项真实回环 HTTP/2 测试验证成功/错误、异常信息边界和超时取消。

进程内测试使用真实 gRPC 分派，但不能证明网络序列化。TCP 测试只绑定 `127.0.0.1` 的随机端口，不依赖外部服务器；测试和示例均在结束时关闭 channel/server 并等待终止。

## 运行演示

在根目录按顺序执行。必须带 `-am`，将共享 contract 一起构建并复制到运行时依赖目录：

```bash
mvn -B -pl exercises/rpc-basics/solution -am package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
java -cp 'exercises/rpc-basics/solution/target/classes:exercises/rpc-basics/solution/target/dependency/*' io.github.javamastery.exercises.rpc.RpcBasicsDemo
```

Windows 请调整 classpath 分隔符。关键输出：

```text
Remote course: Java RPC 基础
Missing course status: NOT_FOUND
```

演示在一个 JVM 内启动真实 Netty HTTP/2 回环服务和客户端，不是普通 Java 方法互调，也不是两个独立部署的服务。明文仅用于本地教学，不监听外部网卡；没有 TLS、身份认证或服务发现。

## API 与失败约定

服务只提供 GetCourse 单请求单响应。课程 ID 必须为正数，标题非空且最多 200 字符。客户端校验自身输入；服务端仍独立校验请求，不能因为使用生成 stub 就信任调用方。

| 情况 | 行为 |
| --- | --- |
| 非法 ID 到达服务端 | INVALID_ARGUMENT |
| 课程不存在 | NOT_FOUND |
| 目录实现抛出未知运行异常 | 记录服务端异常，返回通用 INTERNAL |
| 调用未在预算内结束 | DEADLINE_EXCEEDED |
| 通道无法连接或服务不可用 | 可出现 UNAVAILABLE，客户端保留原状态 |
| 收到不匹配 ID、空或过长标题 | 客户端拒绝异常响应 |

CourseRpcClient 借用 Channel，不负责关闭。预算必须大于零、不超过 30 秒，每次调用时重新计算 deadline，不把过期 deadline 固定在长生命周期客户端上。示例和测试 channel 使用 disableRetry，客户端没有自行重试循环。

deadline 限制等待时间，不保证取消已开始的数据库写入、外部请求或任意后台任务。真实业务若耗时较长，需要主动检查取消并释放自身资源；本例只读内存目录，不演示业务回滚。不要对所有错误无条件重试，更不能把失败变成空课程。

## 边界与资料

本例没有写 RPC、流式 RPC、负载均衡、注册中心、鉴权、TLS 或分布式事务。错误文本不是稳定机器协议，应依据状态码处理；内部异常只在服务端记录，不能把堆栈或后端连接信息放入响应。Protobuf 字段号不能随意复用，演进协议需保留兼容性。

- [gRPC Java 基础](https://grpc.io/docs/languages/java/basics/)
- [生成代码说明](https://grpc.io/docs/languages/java/generated-code/)
- [Deadline](https://grpc.io/docs/guides/deadlines/)
- [错误处理](https://grpc.io/docs/guides/error/)
- [对应版本官方 Maven 示例](https://github.com/grpc/grpc-java/blob/v1.76.0/examples/pom.xml)

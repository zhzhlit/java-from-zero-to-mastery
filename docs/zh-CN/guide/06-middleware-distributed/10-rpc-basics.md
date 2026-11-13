---
title: RPC 基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-13"
---

# RPC 基础

## 学习目标

- 使用 `.proto` 描述课程查询契约，由工具生成消息和 stub。
- 区分领域目录、RPC 服务适配层、通道和客户端职责。
- 正确处理状态码、deadline 与资源生命周期。
- 区分进程内验证、真实网络调用和生产部署。

## 前置知识与入口

先掌握[网络基础](../03-engineering-foundation/05-network-basics.md)、异常与接口，建议完成[搜索基础](./09-search-basics.md)。本章以独立只读课程目录演示同步 RPC，不把所有调用场景都改成消息队列或远程调用。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/rpc-basics/starter)：补齐服务方法及客户端调用，两个测试类均使用 `@Disabled`。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/rpc-basics/solution)：完整实现与 16 项启用测试。
- [contract](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/rpc-basics/contract)：共享 Protobuf 定义和代码生成配置。
- [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/rpc-basics#readme)：运行演示、版本与边界。

```bash
mvn -B -pl exercises/rpc-basics/starter -am test
mvn -B -pl exercises/rpc-basics/solution -am verify
```

## 契约先于具体类

```proto
service CourseCatalog {
  rpc GetCourse (GetCourseRequest) returns (CourseReply);
}

message GetCourseRequest {
  int64 course_id = 1;
}

message CourseReply {
  int64 course_id = 1;
  string title = 2;
}
```

实际协议还有 package 与 Java 生成配置，见 contract 源文件。Maven 根据固定 gRPC 1.76.0 / protoc 3.25.8 生成消息、服务基类和 blocking stub，配置沿用[对应版本官方示例](https://github.com/grpc/grpc-java/blob/v1.76.0/examples/pom.xml)。共享契约避免 starter、solution 各自复制协议。

消息里的字段号是线上的标识，不能只按 Java 重命名思维修改；删除字段后应保留原编号，不能复用于另一含义。proto3 默认数值是 0，生成类能构造消息不代表业务 ID 合法。

## 单请求单响应链路

```text
调用方 -> CourseRpcClient -> 生成的 blocking stub
  -> Channel -> HTTP/2 + Protobuf
    -> 生成的服务分派 -> CourseRpcService
      -> CourseCatalog 领域接口 -> 内存目录
```

本章不手写传输帧、序列化或调度引擎。InMemoryCourseCatalog 保存不可变快照；服务层负责协议边界，不将网络对象泄漏到领域接口。内存目录用于教学，不冒充数据库持久化。

服务成功时调用一次 onNext，再 onCompleted；失败时 onError 后直接返回，不再发送成功响应。客户端把协议响应还原为 Course，同时验证响应 ID 和标题，不能因为数据来自生成类就无条件信任。

## 状态码不是空值

| 状态 | 本章触发条件 | 调用方含义 |
| --- | --- | --- |
| INVALID_ARGUMENT | 服务端收到非正数 ID | 请求本身不合法 |
| NOT_FOUND | 目录中没有课程 | 本次明确未找到 |
| INTERNAL | 目录实现发生未知运行异常 | 服务端故障，不是未找到 |
| DEADLINE_EXCEEDED | 预算内未完成调用 | 等待终止，执行结果未必可推断 |
| UNAVAILABLE | 服务不可用等传输条件 | 保留状态并按具体场景处理 |

客户端不把这些状态转换成默认课程或空结果。服务内部异常记录在服务端，响应只提供通用描述；真实网络测试确认后端细节和异常 cause 不跨传输返回。状态描述供人阅读，不应按文本匹配分支。

## Deadline 按调用建立

```java
var reply = stub.withDeadlineAfter(timeout.toNanos(), TimeUnit.NANOSECONDS)
        .getCourse(GetCourseRequest.newBuilder().setCourseId(id).build());
```

预算在每次调用前设置；不能在客户端构造时创建一次带绝对 deadline 的 stub，再长期复用。测试先完成一次调用、等待超过预算，再调用同一客户端，验证新调用获得新预算。预算包含调用方等待，不只是服务方法执行时间；参考[官方 deadline 指南](https://grpc.io/docs/guides/deadlines/)。

测试服务故意不返回响应，通过取消监听器验证超时传播，不靠长时间阻塞业务线程模拟慢服务。取消通知不等于杀死任意线程，更不保证回滚数据库或停止外部副作用，实际业务需要协作处理取消。

本章没有自动重试，示例和测试 channel 禁用重试。尤其对写操作，超时可能发生在业务已经成功之后，盲目重试可能重复执行；需另行设计幂等键、查询结果与重试策略，不能仅凭 UNAVAILABLE 就无限重试。

## 验证与资源

13 项进程内/模型测试检查契约编码、输入、状态、错误传播、响应和 deadline；3 项真实 Netty TCP/HTTP/2 测试补充网络序列化、错误信息边界及取消传播。两者都运行在默认 Maven 测试中。

进程内测试不能证明 TLS 或网络代理配置正确。演示和 TCP 测试只绑定 `127.0.0.1` 随机端口，明文仅用于本地教学；演示虽然在一个 JVM 内，但客户端真正经过网络通道，不是直接调用服务方法。

Channel 由创建者持有，CourseRpcClient 只借用。结束时关闭 channel 和 server，并等待资源终止。生产需要另外配置 TLS、鉴权、服务发现、观测性和连接管理；不能把回环明文配置直接用于公网部署。

## 自检与下一步

- 为什么本地客户端校验后，服务端还必须校验 ID？
- 为什么超时和 NOT_FOUND 不能统一为“没有课程”？
- deadline 到期时，服务端业务可能已经成功吗？
- 为什么共享协议不等于共享服务实现类？

下一章：[中间件综合实践](./11-middleware-basics-review.md)，串联权威数据、可靠事件、搜索投影与服务边界。

---
title: 网络基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# 网络基础

## 学习目标

- 理解 URL 中协议、主机、端口、路径和查询串的含义。
- 能从 HTTP 方法和 URL 构造基本请求行。
- 能按状态码族判断一次 HTTP 响应的大致结果。
- 知道查询参数需要解码，并且同一个参数名可以出现多次。
- 初步理解幂等方法、临时失败、超时和重试退避。
- 用 JUnit 5 测试验证网络边界逻辑，而不依赖真实外网。

## 前置知识

已完成[算法入门](./04-algorithms-basics.md)，能用测试描述输入、输出、边界条件和异常场景。

## 练习入口

本章对应仓库中的网络基础练习：

- [`exercises/network-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/network-basics/starter)：学习者编辑区。
- [`exercises/network-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/network-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/network-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/network-basics/solution -am test
```

## 为什么先不真实联网

真实 HTTP 请求会受到网络、DNS、代理、证书和远端服务状态影响。初学阶段更适合先把可确定的规则拆开练：

1. 一个 URL 可以被稳定解析。
2. 查询参数可以被稳定解码。
3. 状态码可以按范围稳定分类。
4. 是否重试可以按方法和状态码稳定判断。

这些规则掌握后，再学习 `java.net.http.HttpClient` 会轻松很多。

## URL 和 URI

常见 URL 形如：

```text
https://example.com:443/courses/java?level=beginner
```

它包含：

| 部分 | 示例 | 说明 |
| --- | --- | --- |
| 协议 | `https` | 说明使用哪种通信规则 |
| 主机 | `example.com` | 说明访问哪台服务 |
| 端口 | `443` | 说明服务监听的位置，HTTP 默认 `80`，HTTPS 默认 `443` |
| 路径 | `/courses/java` | 说明访问服务中的哪个资源 |
| 查询串 | `level=beginner` | 传递筛选、分页、搜索等参数 |

练习中的 `UrlInspector` 使用标准库解析 URL，并为 HTTP/HTTPS 补默认端口。

## 查询参数

查询串不是简单的“等号字符串”。它有几个常见边界：

- 同一个参数名可以出现多次，例如 `tag=java&tag=http`。
- 参数值可以为空，例如 `empty=`。
- 没有等号的参数也常被视为空值，例如 `flag`。
- `+` 和 `%20` 常表示空格，需要解码。

练习中的 `QueryParameters` 会把查询串解析成 `Map<String, List<String>>`，保留参数顺序和重复值。

## HTTP 请求行

HTTP/1.1 请求报文的第一行通常包含方法、请求目标和协议版本：

```text
GET /courses?page=2 HTTP/1.1
```

练习中的 `HttpRequestLine` 从方法和 URL 构造这行文本。注意请求目标一般使用路径和查询串，不包含协议和主机。

## HTTP 状态码

状态码族能快速说明响应大类：

| 范围 | 名称 | 常见含义 |
| --- | --- | --- |
| `1xx` | 信息 | 请求仍在处理中 |
| `2xx` | 成功 | 请求已被成功处理 |
| `3xx` | 重定向 | 需要访问另一个位置 |
| `4xx` | 客户端错误 | 请求本身可能有问题 |
| `5xx` | 服务端错误 | 服务端处理失败 |

练习中的 `HttpStatusClassifier` 会把范围外状态码归为 `UNKNOWN`。

## 重试边界

重试不是“失败了就再试”。通常需要同时考虑：

- 方法是否幂等：`GET`、`HEAD`、`PUT`、`DELETE` 通常比 `POST` 更适合默认重试。
- 失败是否临时：`408`、`429`、`502`、`503`、`504` 比 `400`、`404` 更像临时问题。
- 是否有退避：连续快速重试可能放大故障，应该逐步拉开间隔。

练习中的 `RetryPolicy` 只做一个小规则集，帮助你先建立“能不能重试”的判断框架。

## 分级练习

- **基础**：完成 `UrlInspector` 和 `HttpStatusClassifier`，能读懂 URL 与状态码。
- **进阶**：完成 `QueryParameters` 和 `HttpRequestLine`，处理查询串和请求目标。
- **挑战**：完成 `RetryPolicy`，说明为什么 `POST 503` 默认不重试，而 `GET 503` 可以重试。

## 常见错误与排查

- 把整个 URL 放进请求行目标：HTTP/1.1 常见请求目标应是路径和查询串。
- 忘记默认端口：`https://example.com` 没写端口，但默认是 `443`。
- 把查询参数解析成单值：重复参数会丢数据。
- 把所有 `4xx` 都重试：客户端错误通常需要先修请求。
- 立即无限重试：可能让已经繁忙的服务更难恢复。

## 面试与复习题

1. URL 的协议、主机、端口、路径分别解决什么问题？
2. 为什么查询参数适合表示筛选、分页和搜索条件？
3. `2xx`、`4xx`、`5xx` 分别代表什么响应大类？
4. 为什么 HTTP 请求行里通常不写协议和主机？
5. 什么是幂等方法？它和重试有什么关系？
6. 为什么重试需要退避，而不是马上连续重试？

## 本章总结

网络基础的第一步不是马上写客户端，而是先能稳定解释 URL、请求、响应和重试边界。完成本章后，你已经能把常见 HTTP 规则拆成可测试的 Java 代码。

## 下一步

上一章：[算法入门](./04-algorithms-basics.md)

下一章：[操作系统基础](./06-operating-system-basics.md)

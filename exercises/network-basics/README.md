# 网络基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的[网络基础](../../docs/zh-CN/guide/03-engineering-foundation/05-network-basics.md)，再进入 `starter`。本练习不访问真实网络，重点是理解 URL、HTTP 请求/响应和重试决策的可测试规则。

验证 starter：

```bash
mvn -B -pl exercises/network-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/network-basics/solution -am test
```

## 练习

1. `UrlInspector`：解析 URL 的协议、主机、端口、路径和查询串。
2. `QueryParameters`：解析查询参数，覆盖重复键、空值、无等号参数和 URL 解码。
3. `HttpStatusClassifier`：把状态码归类为信息、成功、重定向、客户端错误、服务端错误或未知。
4. `HttpRequestLine`：从 HTTP 方法和 URL 构造 HTTP/1.1 请求行。
5. `RetryPolicy`：判断幂等方法遇到临时失败时是否适合重试，并计算简单退避延迟。

## 推荐顺序

1. 先完成 `UrlInspector`，弄清 URL 的各个组成部分。
2. 再完成 `QueryParameters`，理解查询串不是普通字符串拼接。
3. 完成 `HttpStatusClassifier` 和 `HttpRequestLine`，把请求和响应放到一起看。
4. 最后完成 `RetryPolicy`，讨论哪些失败可以重试、哪些请求默认不应重试。

## 完成标准

完成本练习后，你应该能做到：

- 解释 URL 中协议、主机、端口、路径和查询串的作用。
- 使用状态码族判断一次 HTTP 响应的大致结果。
- 写出基本 HTTP/1.1 请求行。
- 说明幂等方法、临时失败和重试退避之间的关系。
- 用单元测试覆盖网络边界逻辑，而不依赖真实外网。

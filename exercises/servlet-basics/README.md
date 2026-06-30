# Servlet 基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的 [Servlet 基础](../../docs/zh-CN/guide/04-database-web/04-servlet-basics.md)，再进入 `starter`。本练习用纯 Java 模拟 Servlet 核心边界，重点是掌握请求对象、响应对象、方法分派、过滤器链和路径映射。

验证 starter：

```bash
mvn -B -pl exercises/servlet-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/servlet-basics/solution -am test
```

## 练习

1. `WebRequest`：保存方法、路径、参数、头部和正文，并提供读取方法。
2. `WebResponse`：维护状态码、响应头和可追加正文。
3. `MiniServlet`：在 `service` 中按方法分派到 `doGet` 或 `doPost`。
4. `ServletFilter` 与 `FilterChain`：让过滤器能在 servlet 前后处理请求。
5. `ServletDispatcher`：按路径找到 servlet，找不到时返回 404。

## 完成标准

完成本练习后，你应该能做到：

- 说明 Servlet 为什么把 HTTP 请求包装成请求对象。
- 说明 `service`、`doGet` 和 `doPost` 的分工。
- 使用响应对象设置状态码、头部和正文。
- 解释过滤器链为什么能做日志、认证和跨域等横切逻辑。
- 说明 Dispatcher 如何把路径映射到具体 servlet。

# HTTP 基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的 [HTTP 基础](../../docs/zh-CN/guide/04-database-web/03-http-basics.md)，再进入 `starter`。本练习不引入服务器框架，重点是掌握 HTTP 请求、响应、头部、正文、状态码和路由分派的边界。

验证 starter：

```bash
mvn -B -pl exercises/http-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/http-basics/solution -am test
```

## 练习

1. `HttpRequest`：保存请求方法、路径、请求头和正文，并提供头部读取方法。
2. `HttpRequestParser`：解析 HTTP/1.1 请求行、请求头和正文。
3. `HttpResponse`：保存状态码、原因短语、响应头和正文，并提供常用响应工厂方法。
4. `HttpResponseWriter`：写出 HTTP/1.1 响应文本，并自动补齐 `Content-Length`。
5. `RouteKey` 与 `SimpleRouter`：按方法和路径分派请求，找不到路由时返回 404。

## 完成标准

完成本练习后，你应该能做到：

- 说明 HTTP 请求行、请求头、空行和正文的边界。
- 说明 HTTP 响应状态行、响应头、空行和正文的边界。
- 使用状态码表达处理结果，而不是只返回字符串。
- 按请求方法和路径分派到不同处理函数。
- 解释为什么 `Content-Length` 应按字节数计算。

---
title: HTTP 基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# HTTP 基础

## 学习目标

- 理解 HTTP 在浏览器、客户端和 Web 服务之间的位置。
- 能区分请求行、请求头、空行和请求正文。
- 能区分响应状态行、响应头、空行和响应正文。
- 能使用状态码表达成功、创建、客户端错误和未找到等结果。
- 能按请求方法和路径把请求分派给不同处理函数。
- 能解释 `Content-Length` 为什么要按 UTF-8 字节数计算。

## 前置知识

已完成 [JDBC 基础](./02-jdbc-basics.md)，知道 Java 程序如何通过 Repository 访问数据。HTTP 基础会把学习边界从“本地方法调用”推进到“客户端请求服务端资源”。

## 练习入口

本章对应仓库中的 HTTP 基础练习：

- [`exercises/http-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/http-basics/starter)：学习者编辑区。
- [`exercises/http-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/http-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/http-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/http-basics/solution -am test
```

## HTTP 是什么

HTTP 是客户端和服务端交换资源的文本协议。浏览器访问页面、前端调用后端接口、命令行工具请求 JSON 数据，底层都会形成类似的请求和响应。

一个最小请求通常包含：

```http
GET /courses HTTP/1.1
Host: example.com

```

第一行叫请求行，包含方法、路径和协议版本。后面是请求头。请求头之后有一个空行，空行之后才是正文。

## 请求方法与路径

常见方法包括：

- `GET`：读取资源。
- `POST`：提交数据或创建资源。
- `PUT`：整体替换资源。
- `PATCH`：局部修改资源。
- `DELETE`：删除资源。

路径表示要访问的资源位置，例如 `/courses`、`/courses/42`。服务端通常会同时看方法和路径：`GET /courses` 和 `POST /courses` 是两个不同意图。

## 请求头与正文

请求头描述这次请求的元数据，例如：

- `Host`：目标主机。
- `Content-Type`：正文格式。
- `Accept`：客户端希望接收的响应格式。
- `Authorization`：认证信息。

正文承载提交的数据。`GET` 请求通常没有正文，`POST` 和 `PUT` 请求常见 JSON、表单或文件内容。

## 响应状态

响应第一行叫状态行：

```http
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 2

OK
```

状态码按大类理解：

- `2xx`：成功，例如 `200 OK`、`201 Created`。
- `3xx`：重定向。
- `4xx`：客户端请求有问题，例如 `400 Bad Request`、`404 Not Found`。
- `5xx`：服务端处理失败，例如 `500 Internal Server Error`。

## 路由分派

Web 应用不会把所有请求写在一个方法里，而是按方法和路径分派：

```java
SimpleRouter router = new SimpleRouter()
        .get("/health", request -> HttpResponse.ok("UP"))
        .post("/courses", request -> new HttpResponse(201, "Created", Map.of(), request.body()));
```

这段代码表达了两个入口：

- `GET /health` 返回健康检查结果。
- `POST /courses` 处理课程创建请求。

真实框架会提供注解或配置完成同样的分派，本章先用小型 Router 看清底层边界。

## Content-Length

`Content-Length` 表示正文的字节数，不是 Java 字符数。中文、emoji 等字符在 UTF-8 中可能占多个字节：

```java
"你好".getBytes(StandardCharsets.UTF_8).length // 6
```

写响应时自动补齐 `Content-Length`，可以让客户端知道应该读取多少正文数据。

## 分级练习

- **基础**：完成 `HttpRequest` 和 `HttpRequestParser`，能解析请求行、请求头和正文。
- **进阶**：完成 `HttpResponse` 和 `HttpResponseWriter`，能生成标准响应文本。
- **挑战**：完成 `RouteKey` 和 `SimpleRouter`，按方法与路径分派请求并处理 404。

## 常见错误与排查

- 忘记空行：请求头和正文之间必须有空行。
- 把路径和完整 URL 混用：服务端收到的请求目标通常是 `/path` 或 `/path?query`。
- 忽略方法：同一路径的 `GET` 和 `POST` 不应该混为一个处理。
- 用字符数计算 `Content-Length`：应使用编码后的字节数。
- 只返回正文不返回状态码：客户端需要状态码判断处理结果。

## 面试与复习题

1. HTTP 请求行包含哪三部分？
2. 请求头和请求正文之间为什么需要空行？
3. `GET /courses` 和 `POST /courses` 的语义有什么区别？
4. `404` 和 `500` 分别表示哪一侧的问题？
5. 为什么 `Content-Length` 不能直接使用 Java 字符串长度？
6. Web 框架的路由机制本质上在匹配什么？

## 本章总结

HTTP 基础的重点是看清文本协议边界：请求如何表达意图，响应如何表达结果，路由如何把外部请求映射到内部处理函数。完成本章后，你已经能理解 Web 框架在方法、路径、头部、正文和状态码之上做了哪些封装。

## 下一步

上一章：[JDBC 基础](./02-jdbc-basics.md)

继续按照[学习路线](../../roadmap/index.md)进入 Servlet 与 Web 基础。

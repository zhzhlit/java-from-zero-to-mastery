---
title: Servlet 基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# Servlet 基础

## 学习目标

- 理解 Servlet 在 HTTP 协议和 Java Web 应用之间的位置。
- 能说明请求对象、响应对象和 `service` 方法的职责。
- 能区分 `doGet`、`doPost` 和默认 405 响应。
- 能用 Dispatcher 按路径找到对应处理对象。
- 能解释过滤器链的执行顺序和常见用途。
- 能把 Servlet 思维连接到后续 Spring MVC 控制器。

## 前置知识

已完成 [HTTP 基础](./03-http-basics.md)，知道请求行、请求头、正文、状态码和路由分派的边界。Servlet 基础会把“手写 HTTP 文本处理”推进到“由容器包装请求并调用 Java 对象”。

## 练习入口

本章对应仓库中的 Servlet 基础练习：

- [`exercises/servlet-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/servlet-basics/starter)：学习者编辑区。
- [`exercises/servlet-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/servlet-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/servlet-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/servlet-basics/solution -am test
```

## Servlet 解决什么问题

上一章看到，HTTP 是文本协议。真实 Web 应用不希望每个业务方法都手写解析请求行、请求头、空行和正文。Servlet 容器会完成底层网络接收、协议解析和对象包装，然后调用应用提供的 Java 对象。

可以把一次请求理解成这条链路：

```text
HTTP 文本 -> 容器解析 -> Request/Response 对象 -> Servlet.service -> doGet/doPost -> 响应写回
```

本章练习用 `MiniServlet` 模拟这个核心模型，不引入外部 Web 容器。

## 请求与响应对象

Servlet 中的请求对象负责提供当前请求的上下文，例如：

- HTTP 方法和路径。
- 查询参数或表单参数。
- 请求头。
- 请求正文。

响应对象负责写出处理结果，例如：

- 状态码：`200`、`201`、`404`、`405`。
- 响应头：`Content-Type`、`Location` 等。
- 响应正文。

练习中的 `WebRequest` 和 `WebResponse` 就是这两个角色的简化版本。

## service 与方法分派

Servlet 的统一入口是 `service`。它会根据 HTTP 方法把请求交给更具体的方法：

```java
public void service(WebRequest request, WebResponse response) {
    switch (request.method().toUpperCase()) {
        case "GET" -> doGet(request, response);
        case "POST" -> doPost(request, response);
        default -> methodNotAllowed(response);
    }
}
```

业务 servlet 通常只覆盖自己需要的方法：

```java
MiniServlet servlet = new MiniServlet() {
    @Override
    protected void doGet(WebRequest request, WebResponse response) {
        response.write("courses");
    }
};
```

如果请求方法没有被支持，应该返回 `405 Method Not Allowed`，而不是静默成功。

## Dispatcher 映射

一个应用会有多个处理对象。Dispatcher 负责按路径找到 servlet：

```java
ServletDispatcher dispatcher = new ServletDispatcher()
        .register("/health", healthServlet)
        .register("/courses", courseServlet);
```

当请求路径是 `/courses` 时，Dispatcher 调用课程 servlet；路径不存在时返回 `404`。这和上一章 Router 的思想一致，只是处理目标从函数变成了更接近 Web 容器的 servlet 对象。

## 过滤器链

过滤器适合处理“很多请求都需要”的横切逻辑：

- 访问日志。
- 登录认证。
- 权限检查。
- 跨域响应头。
- 统一编码或跟踪 ID。

过滤器通常可以在 servlet 前后执行：

```java
dispatcher.addFilter((request, response, chain) -> {
    response.header("X-Trace", "on");
    chain.doFilter(request, response);
});
```

调用 `chain.doFilter` 之前是前置处理，之后是后置处理。不调用它，请求就不会继续进入下一个过滤器或 servlet。

## 与 Spring MVC 的关系

Spring MVC 不要求你直接写 Servlet，但它仍然运行在 Servlet 模型之上。控制器方法、拦截器、过滤器、请求参数绑定和响应写出，本质上都和本章的这些边界有关。

先掌握 Servlet 思维，后续学习 Spring Boot 时就能更容易理解：

- 为什么控制器方法能拿到请求参数。
- 为什么响应状态码和响应体可以分开设置。
- 过滤器、拦截器和控制器分别适合放什么逻辑。

## 分级练习

- **基础**：完成 `WebRequest` 和 `WebResponse`，能读请求并写响应。
- **进阶**：完成 `MiniServlet`，按 HTTP 方法分派到 `doGet` 或 `doPost`。
- **挑战**：完成过滤器链和 `ServletDispatcher`，支持路径映射、404 和前后置过滤。

## 常见错误与排查

- 把所有请求都塞进一个方法：应按职责拆到不同 servlet 或不同方法。
- 忘记 405：路径存在但方法不支持时，不应该返回 404。
- 过滤器没有调用 `chain.doFilter`：后续过滤器和 servlet 都不会执行。
- 在过滤器里写业务主体：过滤器适合横切逻辑，不适合承载核心业务。
- 直接拼接响应文本：使用响应对象更容易统一状态码、头部和正文。

## 面试与复习题

1. Servlet 容器在调用业务代码前通常做了哪些事情？
2. `service`、`doGet`、`doPost` 分别负责什么？
3. 404 和 405 的区别是什么？
4. 过滤器链为什么适合做日志和认证？
5. 不调用 `chain.doFilter` 会发生什么？
6. Spring MVC 控制器和 Servlet 模型有什么关系？

## 本章总结

Servlet 基础的重点是把 HTTP 请求交给 Java Web 应用的过程看清楚：容器包装请求，Dispatcher 找到处理对象，过滤器处理横切逻辑，Servlet 按方法执行业务并写响应。完成本章后，你已经具备进入真实 Web 容器和 Spring MVC 的必要上下文。

## 下一步

上一章：[HTTP 基础](./03-http-basics.md)

下一章：[Web 基础综合复盘](./05-web-basics-review.md)

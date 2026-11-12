---
title: Spring Security 基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# Spring Security 基础

## 学习目标

- 区分认证、授权与 CSRF 防护。
- 通过 SecurityFilterChain 声明公开、登录及角色访问规则。
- 使用 PasswordEncoder 编码密码，而不是保存明文。
- 根据当前身份访问学习进度，不信任请求体声明的用户名。
- 验证登录会话、拒绝路径和注销行为。

## 前置知识

已完成 [Spring Boot 日志与请求追踪](./06-spring-boot-logging.md)，熟悉 HTTP、Controller 与配置。本章独立模块专注安全过滤器链，使用 Java 21、Spring Boot 3.3.7 与其 BOM 管理的 Spring Security 6.3.6，不改变仓库技术基线。这是指定版本的教学示例，不代表建议生产系统长期停留在这些版本。

## 练习入口

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-security/starter)：安全链与密码编码器骨架，初始不能启动，12 项测试禁用。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-security/solution)：参考实现和 12 项测试。

```bash
mvn -B -pl exercises/spring-boot-security/starter -am test
mvn -B -pl exercises/spring-boot-security/solution -am test
```

完成 starter 后移除测试类上的 `@Disabled`。密码与运行命令见 [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-security#readme)。

## 三个不同的问题

- **认证**：你是谁？本例通过用户名、密码登录，框架建立会话。
- **授权**：你能做什么？登录后的 student 不能访问管理员接口。
- **CSRF 防护**：携带浏览器自动附加的会话 Cookie，不等于修改操作就来自可信页面；还需要校验 CSRF token。

这些不是一个开关。登录成功不等于拥有全部权限；带 CSRF token 也不等于拥有 ADMIN 角色。

## 请求授权规则

新增 `spring-boot-starter-security`，测试新增 `spring-security-test`，版本都由现有 Boot BOM 管理，不手写过滤器代替框架核心安全逻辑。

安全配置的关键规则为：

```java
http.authorizeHttpRequests(authorize -> authorize
        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
        .requestMatchers(HttpMethod.GET, "/courses", "/csrf").permitAll()
        .requestMatchers(HttpMethod.GET, "/profile").authenticated()
        .requestMatchers(HttpMethod.GET, "/admin/summary").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, "/progress").hasRole("STUDENT")
        .requestMatchers(HttpMethod.POST, "/progress").hasRole("STUDENT")
        .anyRequest().denyAll());
```

完整配置还启用默认表单登录页和注销，并保留默认 CSRF。规则按声明顺序匹配，要先写精确边界，再收敛到默认拒绝。`hasRole("ADMIN")` 对应 `ROLE_ADMIN` 权限，不要再把 `ROLE_` 前缀写进 hasRole 参数。

ERROR 分派是容器处理已有错误的内部请求，不等于公开所有 `/error` 的直接访问。允许错误分派可避免 Controller 的 400 被第二次授权判断覆盖。默认拒绝意味着新增接口需要显式考虑访问规则，而不是自动公开。

官方参考：[请求级授权](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)。官方在线文档可能随版本更新，本项目以 BOM 和实际测试的 6.3.6 行为为准。

## 密码编码与演示账户

`PasswordEncoderFactories.createDelegatingPasswordEncoder()` 返回带算法标识的编码器，本例默认结果以 `{bcrypt}` 开头。验证通过 `matches(raw, encoded)`，不是再次编码后比较字符串；同一个密码多次编码的结果可能不同。

本例两个账户由 `InMemoryUserDetailsManager` 管理：

- `student`：只有 STUDENT。
- `admin`：显式拥有 ADMIN、STUDENT，并没有自动角色继承。

原始密码来自 `LEARNING_STUDENT_PASSWORD`、`LEARNING_ADMIN_PASSWORD`，启动时编码后交给用户管理器；缺失或空值会阻止启动。测试值只用于测试，不是应用默认凭证。

环境变量只是本地注入方式，应用配置仍可能持有原始值。生产用户密码应在注册或修改时编码持久化，结合适当的凭证管理和身份系统，而不是为每次启动传入全部用户密码。

## 登录与会话

访问 `/login` 使用 Spring Security 生成的登录页，登录提交是表单，不是 JSON。成功后转到 `/profile`，返回当前认证身份；后续浏览器请求通过会话继续识别用户。

- 匿名访问 `/courses`：`200`。
- 匿名访问 `/profile`：本例表单登录场景返回 `302`，跳转到 `/login`。
- student 访问 `/admin/summary`：`403`。
- admin 访问 `/admin/summary`：`200`。
- 密码错误：跳转 `/login?error`，不建立已认证会话。

不要把匿名访问一律描述为 401；本章是表单登录应用，不是无状态 Bearer API。客户端类型、入口点和安全配置会影响响应方式。

## 修改数据与 CSRF

登录后通过同源 `GET /csrf` 取得 token、参数名和头名称，并在 `POST /progress` 发送该头及会话 Cookie。框架会检查 token；未携带或伪造时返回 `403`，不会修改进度。

默认登录页自带 CSRF 字段，`POST /logout` 也受保护。认证、注销后不要复用旧 token，应重新获取。本例没有放开跨域，公开 token 获取接口不等于允许其他站点跨域读取会话 token。

不要为了解决一个 403 直接关闭 CSRF。先检查是否已认证、角色是否匹配、token 与会话是否一致以及请求方法是否正确。详见 [官方 CSRF 说明](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)。

## 数据归属不能来自客户端

学习进度存在内存 Map 中，仅供演示。Controller 用 `Principal.getName()` 作为键，请求体只接收完成课时数，范围为 0 到 10000。

即使请求体额外声称 `username: "victim"`，也不能把当前用户的数据写到 victim 名下。角色控制只是第一层；真实业务中通常还要检查租户、资源所有者和操作范围。

本模块不把 UserDetails 直接返回给客户端，避免暴露编码后的密码。日志也不应该记录密码、CSRF token 或会话 Cookie。

## 测试不能只模拟登录

测试通过 Boot 上下文加载完整安全链，没有关闭过滤器。

- 真实 `formLogin()` 测试会验证测试密码并复用得到的会话。
- `user(...).roles(...)` 专注授权边界，它是模拟身份，不证明密码正确。
- `csrf()` 与无 token、非法 token 对照，确认保护确实生效。
- 写入后查询进度，确认数据属于当前身份且被拒绝的请求没有写入。
- POST 注销后检查会话失效，受保护资源重新要求登录。

模拟身份与真实认证的区别见 [官方 MockMvc 用户测试说明](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/authentication.html)。

## 分级练习

- **基础**：实现密码编码器，验证存储值不是明文且 `matches` 成功。
- **进阶**：完成公开、登录、角色和默认拒绝规则，分别用 student 和 admin 验证。
- **挑战**：保留 CSRF，验证修改请求的数据归属、缺少 token 不写入以及注销失效。

## 常见错误与排查

- 登录页也被拒绝：检查表单登录的 `permitAll()`，不要粗暴放行全部路径。
- 登录后仍 403：检查角色前缀、匹配顺序和 CSRF，不把认证与授权混为一谈。
- 运行缺少密码：按 README 设置本地环境变量，不在源码中加入默认密码。
- 测试通过但密码错误：只使用模拟用户不足以证明真实认证，需要保留表单登录测试。
- 错误状态被覆盖：检查 ERROR 分派授权，而不是吞掉 Controller 异常。

## 面试与复习题

1. 认证、授权和 CSRF 各自解决什么问题？
2. 为什么不能通过关闭 CSRF 处理所有 403？
3. `hasRole("ADMIN")` 与 `ROLE_ADMIN` 是什么关系？
4. 为什么密码应调用 matches，而不是再次编码后比较？
5. 模拟用户测试与真实表单登录测试分别证明了什么？
6. 为什么修改进度时必须从当前身份取得用户名？

## 本章总结

使用成熟框架建立认证与授权边界，保留 CSRF，并以拒绝路径和数据状态验证配置。本章不是完整生产安全方案；HTTPS、登录限流、账户生命周期、持久化、多因素认证、OAuth2/JWT 等仍需独立设计，不要把演示应用直接暴露到公网。

## 下一步

上一章：[Spring Boot 日志与请求追踪](./06-spring-boot-logging.md)

继续按照[学习路线](../../roadmap/index.md)进入企业级开发综合复盘等后续主题。

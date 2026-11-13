# Spring Security 基础练习

使用 Spring Boot 3.3.7 管理的 Spring Security 6.3.6，实现同步 Servlet 应用的登录、授权、密码编码和 CSRF 防护。

## 目标与任务

- 基础：实现 `PasswordEncoder`，使用 DelegatingPasswordEncoder 的默认 BCrypt 编码，理解认证与授权的区别。
- 进阶：完成 `SecurityFilterChain`，配置公开接口、登录用户和角色规则，其他路由默认拒绝。
- 挑战：保留 CSRF，验证携带会话但缺少或伪造 token 的修改请求被拒绝；验证进度只能修改当前身份的数据。

`starter` 中安全过滤器链与密码编码器是可编译的练习骨架，未完成时不能启动应用；12 项测试在类级 `@Disabled` 下禁用。`solution` 是完整实现，12 项测试默认执行。

## 接口规则

| 接口 | 规则 |
| --- | --- |
| `GET /courses`、`GET /csrf` | 公开 |
| `GET /profile` | 已登录 |
| `GET /admin/summary` | `ADMIN` |
| `GET /progress`、`POST /progress` | `STUDENT`；POST 还需要 CSRF token |
| `/login` | Spring Security 默认登录页与表单处理 |
| `POST /logout` | 框架处理，保留 CSRF 防护 |
| 其他路由 | 默认拒绝 |

`admin` 显式拥有 `ADMIN` 和 `STUDENT` 两种角色，没有隐式角色继承。框架的 ERROR 分派允许执行，避免应用的 400 被错误页的授权规则覆盖；直接请求未列出的路径仍然拒绝。

## 验证

```bash
mvn -B -pl exercises/spring-boot-security/starter -am test
mvn -B -pl exercises/spring-boot-security/solution -am test
```

预期 starter 跳过 12 项，solution 通过 12 项，覆盖真实密码登录、会话复用、登录失败、密码编码、角色授权、CSRF、数据归属、注销和默认拒绝。

## 本地运行

启动前设置两个不同的本地演示密码，不要使用真实账户密码。以下命令静默读取输入，不把密码写进命令历史：

```bash
read -r -s LEARNING_STUDENT_PASSWORD
export LEARNING_STUDENT_PASSWORD
read -r -s LEARNING_ADMIN_PASSWORD
export LEARNING_ADMIN_PASSWORD
mvn -B -pl exercises/spring-boot-security/solution -am compile
mvn -B -f exercises/spring-boot-security/solution/pom.xml org.springframework.boot:spring-boot-maven-plugin:3.3.7:run
```

密码缺失或为空时启动失败，不使用默认密码兜底。测试通过独立测试属性提供专用值，不需要设置环境变量。

打开 `http://localhost:8080/login`，以 `student` 或 `admin` 和刚设置的密码登录，成功后进入 `/profile`。`student` 访问 `/admin/summary` 应为 `403`，`admin` 应为 `200`。

登录后在同源页面的浏览器控制台执行：

```javascript
const csrf = await fetch('/csrf').then(response => response.json())
await fetch('/progress', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
  body: JSON.stringify({ completedLessons: 3 })
}).then(response => response.json())
```

返回当前用户名与 `completedLessons: 3`。去掉 CSRF 头后应返回 `403`，进度不变。登录、注销后 token 会变化，修改请求前重新获取。不要把 token、密码或会话 Cookie 放进 URL、日志或截图。

## 边界

本例使用内存用户与内存进度，重启后进度丢失；环境变量中的原始密码也不是生产凭证管理方案。它不覆盖注册、找回密码、限流、多因素认证、OAuth2/JWT、跨域、持久化会话或生产部署。HTTP 仅用于本地学习，生产还需要 HTTPS、会话与 Cookie 策略等完整安全设计。不要直接将演示应用暴露到公网。

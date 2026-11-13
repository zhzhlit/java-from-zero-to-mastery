# 企业级开发综合复盘

将 Spring Boot 分层、JDBC、事务、安全与请求日志串成一条课程报名链路。各模块独立，不依赖其他练习的 solution。

## 任务

- 基础：理解已提供的 Controller、Repository、DTO、表结构与参数化 SQL。
- 进阶：完成 Service 报名事务，重复报名时回滚名额扣减。
- 挑战：完成安全链，验证匿名、错误角色、缺少 CSRF 时不写库，学生只能查看自己的报名。

starter 保留 Service 报名与安全链骨架，其他基础设施已提供；12 项测试在类级 `@Disabled` 下禁用。完成两处骨架、添加事务边界后移除禁用标记。solution 默认执行同样的 12 项测试。

## 接口

| 接口 | 权限与行为 |
| --- | --- |
| `GET /courses`、`GET /csrf` | 公开课程、取得 CSRF token |
| `GET /profile` | 当前登录身份 |
| `POST /courses/{id}/enrollments` | STUDENT + CSRF，从 Principal 取得报名人，不接收请求体身份 |
| `GET /enrollments` | STUDENT，只查看自己 |
| `GET /admin/enrollments` | ADMIN，查看全部 |
| `/login`、`POST /logout` | 默认表单登录与带 CSRF 的注销 |

未列出路径默认拒绝。admin 显式拥有 ADMIN、STUDENT 两种角色。事务覆盖条件扣减与唯一约束写入，不在测试上加事务。请求日志过滤器在安全链之前运行，因此登录跳转和安全拒绝也有请求 ID。

## 验证

```bash
mvn -B -pl exercises/enterprise-basics-review/starter -am test
mvn -B -pl exercises/enterprise-basics-review/solution -am test
```

预期 starter 跳过 12 项，solution 通过 12 项。测试不仅看状态码，也检查数据库名额与报名数。

## 运行

沿用 [安全基础练习](../spring-boot-security/README.md) 的方式，静默读取并导出 `LEARNING_STUDENT_PASSWORD`、`LEARNING_ADMIN_PASSWORD`，再运行：

```bash
mvn -B -pl exercises/enterprise-basics-review/solution -am compile
mvn -B -f exercises/enterprise-basics-review/solution/pom.xml org.springframework.boot:spring-boot-maven-plugin:3.3.7:run
```

打开 `http://localhost:8080/login`，以 student 登录；在同源浏览器控制台执行：

```javascript
const csrf = await fetch('/csrf').then(response => response.json())
await fetch('/courses/1/enrollments', {
  method: 'POST',
  headers: { [csrf.headerName]: csrf.token, 'X-Request-Id': 'review-demo' }
}).then(response => response.status)
```

首次为 `201`，再次为 `409`；`GET /courses` 显示课程 1 仍剩 1 个名额，`GET /enrollments` 只有自己的 1 条记录。去掉 token 返回 `403`，不写库。不要在请求 ID、URL 或日志中放密码、token 或 Cookie。

H2 与用户均为内存存储，重启后重置；仅用于本地学习，不是生产部署模板，不覆盖持久化身份、跨服务追踪、分布式事务或外部数据库兼容性。

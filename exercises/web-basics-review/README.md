# Web 基础综合练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的 [Web 基础综合复盘](../../docs/zh-CN/guide/04-database-web/05-web-basics-review.md)，再进入 `starter`。本练习用纯 Java 串联数据库、JDBC、HTTP 和 Servlet 阶段的核心边界，构建一个小型课程 Web API。

验证 starter：

```bash
mvn -B -pl exercises/web-basics-review/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/web-basics-review/solution -am test
```

## 练习

1. `InMemoryCourseStore`：模拟 Repository，支持发布课程列表、按 id 查询和创建草稿课程。
2. `ApiRequest` / `ApiResponse`：表达 HTTP 请求输入和响应输出。
3. `CourseController`：实现课程列表、详情和创建接口。
4. `AuthFilter`：保护写操作，缺少 Bearer token 时返回 401。
5. `CourseWebApplication`：按方法和路径分派请求，处理 404 和 405。

## 完成标准

完成本练习后，你应该能做到：

- 把数据访问、控制器、过滤器和分派入口拆成清晰边界。
- 使用状态码表达成功、创建、未认证、未找到和方法不允许。
- 说明为什么写操作需要过滤器保护。
- 把前几章的数据库、HTTP 和 Servlet 知识串成一个小型 Web API。

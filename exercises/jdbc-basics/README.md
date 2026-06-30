# JDBC 基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的[JDBC 基础](../../docs/zh-CN/guide/04-database-web/02-jdbc-basics.md)，再进入 `starter`。本练习使用 H2 内存数据库，重点是掌握 JDBC 调用链、参数化语句、结果映射和资源关闭。

验证 starter：

```bash
mvn -B -pl exercises/jdbc-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/jdbc-basics/solution -am test
```

## 练习

1. `DatabaseConfig`：保存 JDBC URL、用户名和密码，并拒绝空 URL。
2. `ConnectionFactory`：使用 `DriverManager` 打开连接，并把连接失败转换成项目异常。
3. `CourseRowMapper`：从 `ResultSet` 读取列并映射成 `CourseRecord`。
4. `JdbcCourseRepository.findById`：用 `PreparedStatement` 按 id 查询一条课程。
5. `JdbcCourseRepository.findPublished`：用参数化查询筛选已发布课程。
6. `JdbcCourseRepository.create`：用 `PreparedStatement` 插入课程。

## 完成标准

完成本练习后，你应该能做到：

- 说明 JDBC 的 `Connection`、`PreparedStatement` 和 `ResultSet` 各自负责什么。
- 使用 `?` 占位符绑定参数，而不是拼接业务值。
- 用 try-with-resources 关闭 JDBC 资源。
- 把查询结果映射成 Java record。
- 把底层 `SQLException` 转成更贴近业务边界的异常。

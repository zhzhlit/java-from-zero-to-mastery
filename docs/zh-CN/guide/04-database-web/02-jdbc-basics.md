---
title: JDBC 基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# JDBC 基础

## 学习目标

- 理解 JDBC 在 Java 程序和关系型数据库之间的位置。
- 能使用 `DriverManager` 打开数据库连接。
- 能用 `PreparedStatement` 绑定参数并执行查询或插入。
- 能从 `ResultSet` 读取列并映射成 Java 对象。
- 能用 try-with-resources 正确关闭 JDBC 资源。
- 能把底层 `SQLException` 转换成项目边界内的异常。

## 前置知识

已完成[数据库基础](./01-database-basics.md)，知道表、列、SQL 文本、参数值和结果映射的基本边界。

## 练习入口

本章对应仓库中的 JDBC 基础练习：

- [`exercises/jdbc-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/jdbc-basics/starter)：学习者编辑区。
- [`exercises/jdbc-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/jdbc-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/jdbc-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/jdbc-basics/solution -am test
```

## JDBC 调用链

JDBC 是 Java 标准库中的数据库访问 API。一次基础查询通常经过这些对象：

1. `Connection`：代表一次数据库连接。
2. `PreparedStatement`：代表一条预编译 SQL 语句。
3. `ResultSet`：代表查询返回的行。
4. Row Mapper：把当前行映射成 Java 对象。

练习使用 H2 内存数据库。它不要求安装外部数据库服务，但仍然走真实 JDBC API。

## 连接配置

JDBC 连接至少需要 URL。用户名和密码根据数据库而定：

```java
DatabaseConfig config = new DatabaseConfig("jdbc:h2:mem:learning", "sa", "");
ConnectionFactory factory = new ConnectionFactory(config);
```

`ConnectionFactory` 负责把配置交给 `DriverManager.getConnection`。调用方不应该在业务代码里散落连接细节。

## 参数化语句

继续沿用上一章的原则：业务值通过 `?` 占位符绑定。

```java
PreparedStatement statement = connection.prepareStatement(
        "SELECT id, title FROM courses WHERE id = ?"
);
statement.setLong(1, id);
```

参数位置从 1 开始。每个 `setXxx` 方法都应该和列类型、Java 值类型相匹配。

## 结果映射

`ResultSet` 指向当前行。读取列后，应尽快映射成项目自己的对象：

```java
CourseRecord course = new CourseRecord(
        resultSet.getLong("id"),
        resultSet.getString("title"),
        resultSet.getInt("lesson_count"),
        resultSet.getBoolean("published")
);
```

把映射逻辑放进 `CourseRowMapper`，可以避免每个查询方法重复读取列名。

## 资源关闭

`Connection`、`PreparedStatement` 和 `ResultSet` 都需要关闭。推荐使用 try-with-resources：

```java
try (Connection connection = factory.openConnection();
     PreparedStatement statement = connection.prepareStatement(sql);
     ResultSet resultSet = statement.executeQuery()) {
    // read rows
}
```

关闭顺序由 Java 自动处理。这样即使查询失败，资源也会被释放。

## Repository 边界

练习中的 `JdbcCourseRepository` 提供三个方法：

- `findById(long id)`：按主键查询一条课程。
- `findPublished(int minimumLessons)`：筛选已发布且课时数达标的课程。
- `create(CourseRecord course)`：插入一条课程。

Repository 对外返回 `CourseRecord`、`Optional` 或列表，对内隐藏 JDBC 细节。

## 异常转换

JDBC 方法通常抛出 `SQLException`。练习会把它转换成 `JdbcAccessException`：

- 连接失败：`connectionFailed`
- 查询或更新失败：`queryFailed`

这样调用方不必知道底层数据库驱动的异常细节。

## 分级练习

- **基础**：完成 `ConnectionFactory`，能打开 H2 内存库连接。
- **进阶**：完成 `findById` 和 `CourseRowMapper`，查询并映射单行。
- **挑战**：完成筛选列表和插入方法，处理多行结果和更新行数。

## 常见错误与排查

- 忘记绑定参数：执行时会报参数未设置。
- 参数下标从 0 开始：JDBC 参数下标从 1 开始。
- 忘记调用 `resultSet.next()`：读取前需要先移动到第一行。
- 返回 JDBC 对象给业务层：业务层应该看到自己的领域对象。
- 手动关闭遗漏：优先使用 try-with-resources。

## 面试与复习题

1. JDBC 中 `Connection`、`PreparedStatement`、`ResultSet` 分别负责什么？
2. 为什么 JDBC 参数位置从 1 开始容易出错？
3. `executeQuery` 和 `executeUpdate` 分别适合什么场景？
4. 为什么 Repository 不应该把 `ResultSet` 暴露给调用方？
5. try-with-resources 如何帮助释放数据库资源？
6. 为什么要把 `SQLException` 转成项目自己的异常？

## 本章总结

JDBC 基础的重点不是写很多 SQL，而是把连接、参数、结果、资源和异常边界放稳。完成本章后，你已经能用 Java 访问真实 JDBC 数据库，并为后续 HTTP、Servlet 和持久化 Web 功能做准备。

## 下一步

上一章：[数据库基础](./01-database-basics.md)

继续按照[学习路线](../../roadmap/index.md)进入 HTTP Servlet 与 Web 基础。

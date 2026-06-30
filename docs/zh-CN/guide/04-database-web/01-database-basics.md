---
title: 数据库基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# 数据库基础

## 学习目标

- 理解关系型数据库中的表、列、行、类型和空值约束。
- 能读懂基础 `CREATE TABLE`、`SELECT` 和 `INSERT` 语句。
- 区分 SQL 标识符、SQL 文本和参数值。
- 了解为什么业务值应通过 `?` 占位符参数化，而不是直接拼进 SQL。
- 掌握 `WHERE`、`ORDER BY`、`LIMIT`、`OFFSET` 的基础用途。
- 用 JUnit 5 测试验证 SQL 边界逻辑，而不依赖真实数据库。

## 前置知识

已完成[操作系统基础](../03-engineering-foundation/06-operating-system-basics.md)，知道程序运行环境、文件和资源边界会影响 Java 应用。

## 练习入口

本章对应仓库中的数据库基础练习：

- [`exercises/database-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/database-basics/starter)：学习者编辑区。
- [`exercises/database-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/database-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/database-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/database-basics/solution -am test
```

## 为什么先不连真实数据库

真实数据库会涉及安装、账号、端口、驱动、连接池和 SQL 方言。初学阶段先把稳定规则拆开：

1. 表结构如何表达。
2. SQL 标识符如何校验。
3. 查询条件和参数如何分离。
4. 分页如何限制返回行数。
5. 查询结果如何映射成 Java 对象。

这些规则能被单元测试稳定验证，之后再学习 JDBC 和 Spring Data 会更顺。

## 表、列和行

关系型数据库常把数据放在表中。表由列定义结构，每一行是一条记录。

以课程表为例：

| 列 | 类型 | 含义 |
| --- | --- | --- |
| `id` | `BIGINT` | 课程编号 |
| `title` | `VARCHAR(120)` | 课程标题 |
| `lesson_count` | `INTEGER` | 课时数量 |
| `published` | `BOOLEAN` | 是否发布 |

练习中的 `TableSchema` 会用列定义生成基础 `CREATE TABLE` 语句，并找出哪些列是必填列。

## SQL 标识符

表名和列名属于 SQL 标识符。它们不是普通用户输入，通常只能从受控代码中选择。

练习中的 `SqlIdentifier` 使用保守规则：

- 必须以字母开头。
- 后续只能包含字母、数字和下划线。
- 不能是常见 SQL 保留字。
- 表名和列名拼接时需要分别校验。

这个练习的目的不是覆盖所有数据库方言，而是建立“标识符和参数值不同”的意识。

## 参数化查询

业务值不要直接拼进 SQL。更好的方式是 SQL 文本使用 `?` 占位符，值单独放入参数列表：

```sql
SELECT id, title FROM courses WHERE published = ? AND lesson_count >= ?
```

参数列表可能是：

```text
[true, 5]
```

这样做可以降低 SQL 注入风险，也方便数据库驱动处理类型转换。

## 查询、排序和分页

常见读取语句会组合几个部分：

```sql
SELECT id, title
FROM courses
WHERE published = ?
ORDER BY title ASC
LIMIT 20 OFFSET 40
```

- `WHERE`：过滤行。
- `ORDER BY`：确定返回顺序。
- `LIMIT`：限制返回数量。
- `OFFSET`：跳过前面的行。

练习中的 `SelectQueryBuilder` 会构造 SQL 文本和参数列表。

## 插入数据

插入语句同样应该把 SQL 文本和值分开：

```sql
INSERT INTO courses (title, lesson_count, published) VALUES (?, ?, ?)
```

参数列表按列顺序保存：

```text
["Java 入门", 12, false]
```

练习中的 `InsertStatementBuilder` 会保留插入顺序，并拒绝没有任何值的插入。

## 结果映射

数据库驱动通常会把每一行数据交给 Java 程序。应用需要把这些值映射成领域对象。

练习中的 `CourseRowMapper` 把 `Map<String, Object>` 映射成 `CourseSummary`，并检查：

- 必填列是否存在。
- 数字列是否真的是数字。
- 字符串列是否非空。
- 布尔列是否真的是布尔值。

这类边界检查能让错误更早暴露。

## 分级练习

- **基础**：完成 `SqlIdentifier` 和 `TableSchema`，理解表结构和安全标识符。
- **进阶**：完成 `SelectQueryBuilder` 和 `InsertStatementBuilder`，练习参数化 SQL。
- **挑战**：完成 `CourseRowMapper`，处理数据库行到 Java 对象的类型转换。

## 常见错误与排查

- 把用户输入当作表名或列名直接拼接：标识符必须来自受控范围。
- 把业务值拼进 SQL 文本：应该使用 `?` 占位符和参数列表。
- 忘记 `ORDER BY`：分页结果可能不稳定。
- `LIMIT` 或 `OFFSET` 接收负数：分页参数需要先校验。
- 映射结果时不检查类型：错误可能延迟到业务逻辑里才爆出。

## 面试与复习题

1. 表、列和行分别对应什么概念？
2. `NOT NULL` 约束解决什么问题？
3. SQL 标识符和参数值有什么区别？
4. 为什么业务值应该通过 `?` 占位符传入？
5. `ORDER BY`、`LIMIT`、`OFFSET` 如何配合分页？
6. 把查询结果映射成 Java 对象时为什么要检查类型？

## 本章总结

数据库基础的第一步是能说清楚表结构、SQL 文本、参数值和结果映射边界。完成本章后，你已经能把最常见的 SQL 规则写成小而稳定的 Java 测试。

## 下一步

上一章：[操作系统基础](../03-engineering-foundation/06-operating-system-basics.md)

继续按照[学习路线](../../roadmap/index.md)进入 JDBC、HTTP Servlet 与 Web 基础。

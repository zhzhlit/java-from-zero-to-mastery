# 数据库基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的[数据库基础](../../docs/zh-CN/guide/04-database-web/01-database-basics.md)，再进入 `starter`。本练习不连接真实数据库，重点是理解表结构、SQL 参数化和结果映射的可测试规则。

验证 starter：

```bash
mvn -B -pl exercises/database-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/database-basics/solution -am test
```

## 练习

1. `SqlIdentifier`：校验表名和列名，拒绝空白、非法字符和常见保留字。
2. `TableSchema`：用列定义生成 `CREATE TABLE`，识别必填列和重复列。
3. `SelectQueryBuilder`：构造带条件、排序、分页和参数列表的 `SELECT`。
4. `InsertStatementBuilder`：构造带占位符和有序参数的 `INSERT`。
5. `CourseRowMapper`：把数据库行 `Map<String, Object>` 映射成课程摘要对象。

## 推荐顺序

1. 先完成 `SqlIdentifier`，理解 SQL 中哪些名字可以安全拼接。
2. 再完成 `TableSchema`，把表、列、类型和空值约束放在一起看。
3. 完成 `SelectQueryBuilder` 和 `InsertStatementBuilder`，练习使用 `?` 占位符保存参数。
4. 最后完成 `CourseRowMapper`，处理数据库值到 Java 对象的类型边界。

## 完成标准

完成本练习后，你应该能做到：

- 解释表、列、类型、必填约束和重复列的意义。
- 区分 SQL 文本和参数值。
- 构造基础 `SELECT`、`INSERT`、`WHERE`、`ORDER BY`、`LIMIT` 和 `OFFSET`。
- 说明为什么查询结果映射需要处理缺失列和错误类型。
- 用单元测试保护 SQL 边界逻辑，而不依赖真实数据库。

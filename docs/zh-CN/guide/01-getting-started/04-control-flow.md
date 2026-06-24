---
title: 流程控制
status: verified
javaVersion: "21"
verifiedAt: "2026-06-24"
---

# 流程控制

## 学习目标

- 使用条件、`switch` 表达式和循环表达业务规则。
- 正确处理输入边界和循环终止条件。
- 使用卫语句降低嵌套复杂度。
- 识别无限循环、遗漏分支和 off-by-one 错误。

## 前置知识

已掌握[变量与数据类型](./03-variables-and-data-types.md)。

## 真实问题场景

票价、成绩等级和权限判断都依赖分支；批量统计依赖循环。代码“能走通一条路径”远远不够，还必须覆盖边界和错误输入。

## 核心概念

### 条件判断

```java
if (age < 0) {
    throw new IllegalArgumentException("age must not be negative");
} else if (age <= 5) {
    return 0;
} else {
    return 100;
}
```

### `switch` 表达式

Java 21 可以使用表达式形式直接返回结果：

```java
return switch (score / 10) {
    case 10, 9 -> "A";
    case 8 -> "B";
    default -> "C";
};
```

箭头形式不会像传统 `case` 那样意外贯穿。

### 循环

- `for`：次数或索引明确。
- 增强 `for`：依次访问数组或集合元素。
- `while`：先判断条件，次数不确定。
- `do-while`：至少执行一次。

## 原理与设计思想

先处理无效输入，再处理正常路径，通常比多层嵌套更清楚：

```java
if (score < 0 || score > 100) {
    throw new IllegalArgumentException("invalid score");
}
return calculateGrade(score);
```

循环必须回答三个问题：初始状态是什么、何时继续、每次如何靠近终止条件。

## 可运行代码

`GradeClassifier` 演示输入校验与 `switch` 表达式；`NumberSummation` 演示有边界的 `for` 循环。

```bash
mvn -B -pl examples/java-basics -am test
```

练习 `TicketPrice`：

```bash
mvn -B -pl exercises/java-basics/starter -am compile
mvn -B -pl exercises/java-basics/solution -am test
```

## 常见错误与排查

- `current < limit` 与 `current <= limit` 混淆：先写出应包含的首尾值。
- 循环变量没有变化：可能形成无限循环。
- 分支边界重叠或有空洞：用边界值测试，例如 5、6、17、18。
- `break` 退出了错误层级：避免过深嵌套，必要时提取方法。
- 条件表达式过长：拆成有名称的布尔变量或方法。

## 最佳实践及适用边界

- 无效输入尽早失败。
- 优先选择表达意图最清楚的循环，而不是最短的写法。
- `continue` 可以跳过无效项，但大量使用可能让流程难以追踪。
- 不用分支复制业务规则；重复条件应集中管理。
- 本章不讨论编译器如何优化循环。

## 分级练习

- **基础**：完成不同年龄段票价计算。
- **进阶**：为所有边界值补充测试并处理负数年龄。
- **挑战**：实现一个闰年判断方法，并列出能推翻错误实现的测试数据。

## 面试与复习题

1. `while` 和 `do-while` 的关键区别是什么？
2. `switch` 表达式与传统 `switch` 语句有什么差异？
3. 什么是 off-by-one 错误？
4. 卫语句为什么能降低复杂度？
5. `break` 与 `continue` 分别做什么？
6. 如何系统地测试年龄区间？
7. 什么时候增强 `for` 不合适？

## 本章总结

流程控制的质量取决于边界是否明确、错误输入是否处理，以及代码能否让读者快速看出所有可能路径。

## 下一步

上一章：[变量与数据类型](./03-variables-and-data-types.md)  
下一章：[方法](./05-methods.md)


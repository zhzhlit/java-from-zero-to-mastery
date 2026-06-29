---
title: JUnit 5 专项练习
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-29"
---

# JUnit 5 专项练习

## 学习目标

- 使用 JUnit 5 编写清晰、可重复运行的单元测试。
- 掌握基础断言、异常测试、参数化测试和测试命名。
- 用边界条件测试保护业务规则。
- 理解 `starter` 与 `solution` 双模块练习方式。

## 前置知识

已完成[测试、Maven 与工程化验证](./01-testing-maven-ci.md)，能够运行 Maven 测试命令，并知道测试失败信息会指出测试类、测试方法和断言差异。

## 练习入口

本章对应仓库中的 JUnit 5 专项练习：

- [`exercises/testing-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/testing-basics/starter)：学习者编辑区。
- [`exercises/testing-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/testing-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/testing-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/testing-basics/solution -am test
```

`starter` 中的测试方法默认带有 `@Disabled`。练习时一次移除一个 `@Disabled`，运行测试观察失败信息，再补测试断言或业务实现。

## 基础断言

基础断言回答“实际结果是否等于预期”。例如 `StudySession` 应保存主题、计划分钟数，并生成稳定摘要：

```java
StudySession session = new StudySession("JUnit assertions", 30);

assertAll(
        () -> assertEquals("JUnit assertions", session.topic()),
        () -> assertEquals(30, session.plannedMinutes()),
        () -> assertEquals("JUnit assertions (30 min)", session.summary())
);
```

`assertAll` 的好处是多个相关断言可以一起报告。它适合验证同一个对象的多个可观察结果，但不应该把完全无关的行为塞进同一个测试。

## 异常测试

失败路径也需要测试。构造学习块时，空主题和非正数分钟数都应该被拒绝：

```java
assertThrows(IllegalArgumentException.class, () -> new StudySession(" ", 30));
assertThrows(IllegalArgumentException.class, () -> new StudySession("Assertions", 0));
```

异常测试不要只验证“有异常”。你要先问清楚：哪些输入非法？异常发生后是否会留下错误状态？本练习中的对象构造失败不会产生对象，因此重点是输入边界。

## 参数化测试

当同一条规则需要多组输入时，用参数化测试比复制多个测试方法更清楚。`ScoreGrade` 的分数边界可以这样表达：

```java
@ParameterizedTest(name = "{0} 分 => {1}")
@CsvSource({
        "100, EXCELLENT",
        "90, EXCELLENT",
        "89, PASS",
        "60, PASS",
        "59, RETRY",
        "0, RETRY"
})
void classifiesScoreBoundaries(int score, ScoreGrade expected) {
    assertEquals(expected, ScoreGrade.fromScore(score));
}
```

参数化测试的关键不是“少写代码”，而是让边界数据集中展示。读测试的人应该一眼看到 59/60、89/90 和 0/100 这些临界点。

## 测试命名

好的测试名称描述行为，而不是描述实现细节。本练习使用两层命名：

- 类级 `@DisplayName("StudySession")` 标明被测对象。
- 方法级 `@DisplayName("拒绝空主题")` 标明具体行为。

方法名仍然保留英文，例如 `rejectsBlankTopic`。这样既便于 Java 工具链识别，也能让中文学习者从测试报告中快速理解失败含义。

## 边界条件测试

边界条件通常出现在规则切换的位置：

- 25 分钟是不是一个专注学习块。
- 59 分和 60 分属于不同等级。
- 89 分和 90 分属于不同等级。
- 空学习计划没有最长学习块。
- 关键字匹配应该忽略大小写。

边界测试不是为了凑数量，而是保护最容易写错的判断条件。很多 bug 不是来自“完全不会写”，而是来自 `>` 和 `>=`、空集合和单元素集合、大小写和空白字符这些细节。

## 分级练习

- **基础**：补全 `StudySessionTest`，让主题、分钟数、摘要和异常测试通过。
- **进阶**：补全 `ScoreGradeTest`，用参数化测试覆盖所有分数边界。
- **挑战**：补全 `LearningPlanTest`，验证集合汇总、`Optional` 空值和大小写无关查询。

## 常见错误与排查

- 测试方法名叫 `test1`：失败报告无法帮助定位业务规则。
- 只测 80 分、不测 59/60：边界判断可能写错仍然通过。
- 用 `try/catch` 手写异常测试：容易吞掉异常或漏掉断言，优先使用 `assertThrows`。
- 参数化测试数据太多：每一行都应该服务于一个规则或边界。
- 同时移除多个 `@Disabled`：失败太多时不容易判断当前要修哪一条规则。

## 面试与复习题

1. `assertEquals` 和 `assertTrue` 适合验证什么类型的结果？
2. `assertThrows` 为什么比手写 `try/catch` 更清楚？
3. 参数化测试适合解决什么重复？
4. 为什么 59/60、89/90 比 70、80 更值得测试？
5. 测试名称应该描述实现步骤还是业务行为？
6. 空集合为什么是重要边界？
7. 什么时候应该使用 `assertAll`？什么时候不应该使用？

## 本章总结

JUnit 5 不是只用来证明“代码能跑”。它把业务规则、失败路径和边界值固定下来，让修改变得可验证。完成本章练习后，再回到后续数据结构、算法和 Web 课程时，你会更自然地先写出能说明规则的测试。

## 下一步

上一章：[测试、Maven 与工程化验证](./01-testing-maven-ci.md)

下一章：[数据结构基础](./03-data-structures-basics.md)

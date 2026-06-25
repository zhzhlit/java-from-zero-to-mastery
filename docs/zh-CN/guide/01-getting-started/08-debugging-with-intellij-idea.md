---
title: IntelliJ IDEA 调试
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-25"
---

# IntelliJ IDEA 调试

## 学习目标

- 区分普通运行与调试运行。
- 创建断点并使用单步执行观察程序状态。
- 使用 Variables、Watches 和 Call Stack 定位错误。
- 在 JUnit 测试中复现数组边界、循环边界和字符串比较问题。
- 建立一套可重复的排查流程。

## 前置知识

已完成[字符串与 StringBuilder](./07-strings-and-stringbuilder.md)，并能在 IntelliJ IDEA 中运行 Maven 项目和 JUnit 测试。

## Run 与 Debug 的区别

Run 直接执行程序，适合确认最终结果。Debug 会启动调试器，可以在断点处暂停，查看变量并控制下一条执行语句。

调试不是逐行“看运气”。先用最小输入稳定复现问题，再根据异常、测试失败和变量状态提出假设。

## 创建并管理断点

在编辑器左侧行号栏单击可创建断点，再次单击可删除。右键断点可以设置条件或临时禁用。

建议从这些位置开始：

- `DebuggingScenarios.findFirstIndex` 的 `for` 循环行。
- `DebuggingScenarios.containsName` 的 `if` 判断行。
- `ScoreParser.parse` 读取 `parts[index]` 的行。
- `ScoreStatistics.average` 的返回行。

断点不要一次铺满整个项目。每个断点都应对应一个问题，例如“最后一次循环时 `index` 是多少？”

## 单步执行

- **Step Over**：执行当前行，停在同一调用层的下一行。
- **Step Into**：进入当前行调用的方法。
- **Step Out**：执行完当前方法，返回调用者。
- **Resume Program**：继续运行到下一个断点或程序结束。

调试 `findFirstIndex(new int[]{4, 7, 9}, 9)` 时，预期 `index` 依次为 `0`、`1`、`2`，随后在下标 2 返回。

## Variables、Watches 与 Call Stack

Variables 显示当前栈帧中的局部变量和参数。Watches 用于持续观察表达式，例如：

```text
values.length
values[index]
index < values.length
```

Call Stack 显示调用路径。调试 `ScoreReportTest` 时，在解析器中应看到：

```text
ScoreReportTest.buildsDeterministicReport
→ ScoreReport.build
→ ScoreParser.parse
```

继续到 `ScoreStatistics.average` 时，调用者仍应包含 `ScoreReport.build`。

## 条件断点与表达式求值

数据较多时，可把循环断点条件设置为：

```text
index == values.length - 1
```

这样只在最后一个合法元素暂停。Evaluate Expression 可临时计算 `values[index] == target`，但不要通过修改变量掩盖真实问题。

## 调试 JUnit 测试

打开 `DebuggingScenariosTest`，点击测试方法左侧运行图标并选择 Debug。测试提供了稳定、可重复的输入，比手工反复操作更适合定位边界错误。

```bash
mvn -B -pl examples/java-basics -am \
  -Dtest=DebuggingScenariosTest test
mvn -B -pl exercises/java-basics/solution -am \
  -Dtest=ScoreParserTest,ScoreStatisticsTest,ScoreReportTest test
```

命令行测试与 IDEA 调试互补：测试证明结果，调试器解释执行过程。

## 案例一：数组越界

错误代码：

```java
for (int index = 0; index <= values.length; index++) {
    if (values[index] == target) {
        return index;
    }
}
```

当长度为 3 时，`index` 会到达 3，但合法下标只有 0、1、2。此时访问 `values[3]` 抛出 `ArrayIndexOutOfBoundsException`。

修复：

```java
for (int index = 0; index < values.length; index++) {
```

在循环行设置断点，添加 Watch `index < values.length`。修复后当 `index` 即将成为 3 时，条件为 `false`，不会进入循环体。

## 案例二：循环边界错误

有些边界错误不会抛异常，而是少处理一个元素：

```java
for (int index = 0; index < values.length - 1; index++) {
```

查找最后一个元素 9 时，测试期望返回 2，却得到 -1。调试时观察到 `index` 只经过 0 和 1。

正确实现位于 `DebuggingScenarios.findFirstIndex`：

```java
for (int index = 0; index < values.length; index++) {
```

断点位置：`if (values[index] == target)`。预期 `values[index]` 依次为 4、7、9。

## 案例三：字符串比较错误

错误代码：

```java
if (name == target) {
    return true;
}
```

即使两个字符串都显示为 `"Linus"`，它们也可能是不同对象。`new String("Linus")` 能稳定暴露这个问题。

修复：

```java
if (name.equals(target)) {
    return true;
}
```

在判断行设置断点，同时观察 `name == target` 与 `name.equals(target)`：前者可能为 `false`，后者按内容返回 `true`。

## 一套可重复的排查流程

1. 用最小测试稳定复现问题。
2. 阅读失败断言、异常类型和第一处项目代码栈帧。
3. 写下一个可验证假设，例如“循环多执行了一次”。
4. 在关键条件或状态变化处设置断点。
5. 用 Step Over 观察变量，用 Step Into 验证调用边界。
6. 修复最小原因，不顺手重写无关代码。
7. 重新运行失败测试，再运行相关测试集。

## 分级练习

- **基础**：调试查找最后一个数组元素，记录每次暂停时的 `index`。
- **进阶**：把循环条件临时改成 `<=`，根据异常和 Call Stack 定位越界行，再恢复正确代码。
- **挑战**：调试成绩报告，从 `ScoreReport.build` 进入解析、平均值计算和报告生成，画出调用路径并记录关键变量。

## 面试与复习题

1. Run 与 Debug 的主要区别是什么？
2. Step Over、Step Into、Step Out 分别适合什么情况？
3. 断点为什么应对应明确问题？
4. Variables 与 Watches 有什么区别？
5. Call Stack 如何帮助定位错误来源？
6. 条件断点适合解决什么问题？
7. 为什么 `index <= values.length` 会越界？
8. 为什么字符串内容比较不能使用 `==`？

## 本章总结

调试器让程序的执行状态可见，但真正有效的是“复现—假设—观察—最小修复—回归验证”的循环。数组和字符串错误尤其适合通过测试与断点联合定位。

## 下一步

上一章：[字符串与 StringBuilder](./07-strings-and-stringbuilder.md)

综合练习：[成绩统计器](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/java-basics)

继续查看：[学习路线](../../roadmap/index.md)

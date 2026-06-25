---
title: 字符串与 StringBuilder
status: verified
javaVersion: "21"
verifiedAt: "2026-06-25"
---

# 字符串与 StringBuilder

## 学习目标

- 理解 `String` 不可变性和内容比较。
- 使用常见字符串方法检查、截取、查找和替换文本。
- 区分空字符串、空白字符串与 `null`。
- 使用 `split` 解析简单文本。
- 使用 `StringBuilder` 高效生成多段文本。

## 前置知识

已完成[数组](./06-arrays.md)，能够遍历 `String[]` 和传递数组参数。

## String 的不可变性

`String` 对象创建后内容不能改变。字符串方法返回新字符串：

```java
String original = " Java ";
String stripped = original.strip();
```

`original` 仍然是 `" Java "`。不可变性让字符串易于共享，但循环中反复使用 `+` 可能创建许多临时对象。

## 常用字符串操作

```java
String text = "Java 21";

text.length();             // 7
text.charAt(0);            // 'J'
text.substring(0, 4);      // "Java"
text.contains("21");       // true
text.indexOf("21");        // 5
text.replace("21", "22");  // "Java 22"
text.strip();              // 去除首尾 Unicode 空白
```

调用前先根据方法契约处理 `null`，不要把 `null` 当作普通字符串。

## equals 与 == 的区别

`==` 比较两个引用是否指向同一个对象，`equals` 比较字符串内容：

```java
String left = new String("Java");
String right = "Java";

System.out.println(left == right);      // false
System.out.println(left.equals(right)); // true
```

不区分大小写时使用 `equalsIgnoreCase`。业务判断字符串内容时，不要依赖字符串常量池让 `==` “碰巧成功”。

## 空、空白与 null

```java
String empty = "";
String blank = "   ";
String missing = null;
```

- `empty.isEmpty()` 为 `true`。
- `blank.isBlank()` 为 `true`。
- 对 `missing` 调用任何实例方法都会抛出 `NullPointerException`。

方法应明确哪些状态可以接受。输入文本常用 `text == null || text.isBlank()` 进行拒绝。

## split 与文本解析

成绩文本可以按逗号拆分：

```java
String[] parts = "88, 92,76, 100".split(",", -1);
```

第二个参数 `-1` 会保留末尾缺失项，因此 `"80,90,"` 会得到三个元素，最后一个是空字符串。这样解析器可以识别缺失成绩，而不是悄悄忽略错误。

`split` 的第一个参数是正则表达式。点号、竖线等特殊字符需要转义；本课只使用简单逗号。

## 使用 StringBuilder 生成文本

需要逐段生成报告时，使用 `StringBuilder`：

```java
StringBuilder report = new StringBuilder();
report.append("1. arrays").append('\n')
        .append("2. strings");
String result = report.toString();
```

它支持 `append`、`insert`、`delete`，最后通过 `toString()` 得到不可变字符串。

## 可运行代码

- [`StringInspector`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-basics/src/main/java/io/github/javamastery/basics/strings/StringInspector.java) 展示空白规范化和内容比较。
- [`ReportBuilder`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-basics/src/main/java/io/github/javamastery/basics/strings/ReportBuilder.java) 展示编号报告生成。
- [成绩统计器练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/java-basics) 组合文本解析、数组统计和报告输出。

```bash
mvn -B -pl examples/java-basics -am \
  -Dtest=StringInspectorTest,ReportBuilderTest test
mvn -B -pl exercises/java-basics/solution -am \
  -Dtest=ScoreParserTest,ScoreReportTest test
```

## 常见错误与调试提示

- 使用 `==` 比较内容：改用 `equals`，并测试 `new String("Java")`。
- 忘记处理 `null`：先定义输入契约，再做检查。
- `split` 丢失末尾空项：使用 `split(",", -1)`。
- 循环中反复 `result = result + item`：改用 `StringBuilder`。
- 混淆 `trim` 与 `strip`：Java 11+ 优先考虑能识别 Unicode 空白的 `strip`。

调试解析器时，在读取 `parts[index]` 的行设置断点，观察原始片段、`strip()` 后结果和当前下标。

## 最佳实践及适用边界

- 比较字符串内容使用 `equals` 或 `equalsIgnoreCase`。
- 对外方法明确区分 `null`、空和空白输入。
- 简单、少量拼接可以使用 `+`；循环和报告构建使用 `StringBuilder`。
- 输出格式要固定换行、字段顺序和数字区域设置，便于测试。
- `char` 不等于完整 Unicode 字符；复杂文本处理留到专题课程。

## 分级练习

- **基础**：规范化一句包含重复空白的文本。
- **进阶**：解析逗号分隔的成绩，并拒绝缺失项与非整数。
- **挑战**：生成包含人数、平均分、最高分和最低分的固定格式报告，并为输出编写精确断言。

## 面试与复习题

1. `String` 为什么称为不可变对象？
2. `==` 与 `equals` 比较字符串时有什么区别？
3. `isEmpty` 与 `isBlank` 有什么区别？
4. `strip` 与 `trim` 的关注范围有何不同？
5. `split(",", -1)` 中的 `-1` 有什么作用？
6. 什么时候应使用 `StringBuilder`？
7. `StringBuilder.toString()` 返回什么？
8. 为什么确定性文本格式有利于自动化测试？

## 本章总结

字符串处理的关键是内容比较、输入边界和不可变性。`StringBuilder` 则为循环拼接和报告生成提供了清晰、可测试的工具。

## 下一步

上一章：[数组](./06-arrays.md)

下一章：[IntelliJ IDEA 调试](./08-debugging-with-intellij-idea.md)

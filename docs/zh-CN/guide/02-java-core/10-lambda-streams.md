---
title: Lambda 与 Stream API
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# Lambda 与 Stream API

## 学习目标

- 理解 lambda 表达式适合表示一段可以传递的行为。
- 使用 `Predicate<T>` 表达筛选条件。
- 使用 `stream`、`filter`、`map`、`sorted` 和 `toList` 处理集合。
- 使用方法引用让简单转换更清晰。
- 使用 `mapToInt` 和 `sum` 做数值统计。
- 使用 `max` 和 `Comparator` 找到最佳结果。
- 编写测试验证筛选、排序、统计和非法输入。

## 前置知识

已完成[日期与时间 API](./09-date-time-api.md)，能够阅读集合、泛型、接口默认方法、不可变返回值和 JUnit 断言。

## 为什么需要 Lambda 与 Stream

集合章节已经学过用循环处理列表。循环非常重要，也会长期使用。Stream API 提供了另一种写法：把“对集合做什么”拆成连续的小步骤。

例如从课时列表中找出时长不少于 45 分钟的标题：

```java
lessons.stream()
        .filter(lesson -> lesson.getDurationMinutes() >= 45)
        .map(CourseLesson::getTitle)
        .toList();
```

这段代码从左到右读：

1. 把列表变成流。
2. 保留满足条件的课时。
3. 把课时对象转换成标题。
4. 收集回列表。

Stream 不是为了替代所有循环。它适合表达筛选、转换、排序、统计这类集合流水线。

## Lambda 表达式

Lambda 表达式是一段可以作为值传递的行为：

```java
lesson -> lesson.getDurationMinutes() >= 45
```

左边 `lesson` 是参数，右边是判断逻辑。这个 lambda 可以作为 `Predicate<CourseLesson>` 使用：

```java
public static List<String> lessonTitlesMatching(List<CourseLesson> lessons,
                                                Predicate<CourseLesson> matcher) {
    return lessons.stream()
            .filter(matcher)
            .map(CourseLesson::getTitle)
            .toList();
}
```

调用时可以传入不同条件：

```java
lessonTitlesMatching(lessons, lesson -> lesson.getDurationMinutes() >= 45);
lessonTitlesMatching(lessons, lesson -> lesson.getTitle().contains("API"));
```

方法不用知道具体筛选规则，调用者把规则传进来。这就是 lambda 的价值：让可变化的行为成为参数。

## Stream 流水线

Stream 流水线通常由三部分组成：

- 来源：例如 `lessons.stream()`。
- 中间操作：例如 `filter`、`map`、`sorted`。
- 终止操作：例如 `toList`、`sum`、`max`。

只有终止操作出现时，整条流水线才会真正执行。初学阶段可以先记住：每条 stream 最后都需要一个终止操作。

## filter：筛选元素

`filter` 接收一个返回 `boolean` 的条件：

```java
.filter(lesson -> lesson.getDurationMinutes() >= 45)
```

满足条件的元素留下，不满足的元素被跳过。它对应循环里的 `if`。

`StudyStreamAnalyzer.lessonTitlesMatching` 先检查参数，再执行筛选：

```java
if (matcher == null) {
    throw new IllegalArgumentException("matcher must not be null");
}
return requireLessons(lessons).stream()
        .map(StudyStreamAnalyzer::requireLesson)
        .filter(matcher)
        .map(CourseLesson::getTitle)
        .toList();
```

这里的第一段 `map(StudyStreamAnalyzer::requireLesson)` 用来拒绝列表中的 `null` 元素，避免后续 lambda 在空对象上调用方法。

## map：转换元素

`map` 把一种元素转换成另一种元素：

```java
.map(CourseLesson::getTitle)
```

这里把 `CourseLesson` 转成 `String`。如果原始流是 `Stream<CourseLesson>`，经过这一步后就变成了 `Stream<String>`。

方法引用 `CourseLesson::getTitle` 可以读成“调用每个课时的 `getTitle` 方法”。它等价于：

```java
.map(lesson -> lesson.getTitle())
```

当 lambda 只是调用一个现成方法时，方法引用通常更简洁。

## sorted：排序

排序需要告诉 Java 按什么规则比较元素：

```java
.sorted(Comparator.comparingInt(CourseLesson::getDurationMinutes)
        .reversed()
        .thenComparing(CourseLesson::getTitle))
```

这段排序规则表示：

- 先按课时时长降序排列。
- 如果时长相同，再按标题升序排列。

排序后再取标题：

```java
return lessons.stream()
        .sorted(...)
        .map(CourseLesson::getTitle)
        .toList();
```

## mapToInt：数值统计

如果要统计总分钟数，可以使用 `mapToInt`：

```java
return requireLessons(lessons).stream()
        .map(StudyStreamAnalyzer::requireLesson)
        .mapToInt(CourseLesson::getDurationMinutes)
        .sum();
```

`mapToInt` 会得到专门处理 `int` 的流，后面可以直接调用 `sum`、`average`、`max` 等统计方法。

## max：找到最佳结果

评审结果已经有 `scoreRate`。用 Stream 找最高分结果：

```java
return safeResults.stream()
        .map(StudyStreamAnalyzer::requireResult)
        .max(Comparator.comparingDouble(ReviewResult::scoreRate))
        .orElseThrow()
        .reportLine();
```

`max` 返回的是 `Optional<ReviewResult>`，因为列表可能为空。本示例会先拒绝空列表：

```java
if (safeResults.isEmpty()) {
    throw new IllegalArgumentException("results must not be empty");
}
```

这样 `orElseThrow` 只是表达“前面已经保证有结果”。`Optional` 的完整用法会在后续常用标准库主题里展开。

## toList 的返回值

Java 21 中，`stream().toList()` 返回不可修改列表。测试可以验证调用者不能继续添加元素：

```java
List<String> titles = StudyStreamAnalyzer.lessonTitlesMatching(lessons,
        lesson -> lesson.getDurationMinutes() >= 45);

assertThrows(UnsupportedOperationException.class,
        () -> titles.add("Stream API"));
```

这和前面课程的“只读返回”保持一致：查询方法返回结果，不让调用者误改内部或结果集合。

## 可运行代码

- [`StudyStreamAnalyzer`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyStreamAnalyzer.java) 展示 lambda、`Predicate`、`filter`、`map`、`sorted`、`mapToInt`、`max` 和方法引用。
- [`StudyStreamAnalyzerTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/StudyStreamAnalyzerTest.java) 覆盖筛选、排序、统计、通过报告、最佳报告、不可修改结果和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 把 Stream 当成必须使用的写法：简单循环仍然完全可以。
- 写了中间操作却没有终止操作：流水线不会真正执行。
- 在 `map` 里做复杂副作用：`map` 应该主要用于转换。
- 忘记处理空列表：`max`、`average` 等操作可能没有结果。
- 让 lambda 变得过长：逻辑复杂时，抽成普通方法再用方法引用。
- 在 Stream 中遇到 `null` 元素：先决定业务是否允许，示例里选择拒绝。

调试 Stream 时，可以把流水线拆成几步临时变量，或先用循环写出等价逻辑，再逐步改成 `filter`、`map`、`sorted`。

## 最佳实践及适用边界

- 用 lambda 表达短小、清楚的条件或转换。
- 当 lambda 只是调用已有方法时，优先考虑方法引用。
- Stream 适合筛选、映射、排序和聚合。
- 需要提前退出、复杂状态更新或多步调试时，循环可能更直观。
- Stream 里尽量避免修改外部变量。
- 初学阶段先掌握顺序流，不急着使用并行流。

## 分级练习

- **基础**：实现 `lessonTitlesMatching`，使用 `Predicate<Lesson>` 和 `filter` 筛选课时标题。
- **进阶**：实现 `lessonTitlesByDurationDescending` 和 `totalDurationMinutes`，练习排序与数值统计。
- **挑战**：实现 `passingReportLines` 和 `bestReportLine`，用方法引用处理评审结果。

## 面试与复习题

1. Lambda 表达式适合表达什么？
2. `Predicate<T>` 的返回值是什么？
3. Stream 流水线通常由哪三部分组成？
4. `filter` 和 `map` 的区别是什么？
5. 方法引用和 lambda 有什么关系？
6. 为什么 `max` 可能需要处理空结果？
7. `stream().toList()` 的结果能否继续修改？
8. 什么场景下循环比 Stream 更合适？

## 本章总结

Lambda 让行为可以作为参数传递，Stream 让集合处理可以写成清晰流水线。掌握 `filter`、`map`、`sorted`、`mapToInt`、`max` 和方法引用后，很多筛选、转换、排序和统计代码都能写得更紧凑、更容易表达意图。

## 下一步

上一章：[日期与时间 API](./09-date-time-api.md)

继续查看：[学习路线](../../roadmap/index.md)

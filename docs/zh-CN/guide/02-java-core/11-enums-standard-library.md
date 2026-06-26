---
title: 枚举与常用标准库
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 枚举与常用标准库

## 学习目标

- 使用 `enum` 表达固定选项集合。
- 为枚举添加字段、构造器和业务方法。
- 使用 `Optional<T>` 表达可能不存在的查询结果。
- 使用 `Objects.requireNonNull` 做必填参数校验。
- 使用 `StringJoiner` 生成稳定摘要文本。
- 使用 `Comparator` 描述排序规则。
- 使用 `EnumMap` 按枚举键统计数据。

## 前置知识

已完成[Lambda 与 Stream API](./10-lambda-streams.md)，能够阅读 `stream`、`filter`、`sorted`、`Optional`、方法引用和 JUnit 断言。

## 为什么需要枚举

学习任务有固定状态：

- 待开始。
- 进行中。
- 已完成。
- 受阻。

如果用字符串表达状态，代码很容易写错：

```java
String status = "todo";
String anotherStatus = "TODO";
String typo = "todoo";
```

这些值都能编译通过，但业务含义并不一致。枚举把固定选项写进类型系统：

```java
StudyTaskStatus status = StudyTaskStatus.TODO;
```

这样调用者只能从定义好的常量中选择，拼写错误会在编译期暴露。

## 定义枚举

枚举使用 `enum` 定义：

```java
public enum StudyTaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    BLOCKED
}
```

枚举常量通常使用全大写命名。它们不是普通字符串，而是 `StudyTaskStatus` 类型的固定对象。

## 枚举字段和方法

枚举也可以有字段、构造器和方法：

```java
public enum StudyTaskStatus {
    TODO("待开始"),
    IN_PROGRESS("进行中"),
    DONE("已完成"),
    BLOCKED("受阻");

    private final String displayName;

    StudyTaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean open() {
        return this != DONE;
    }
}
```

这里 `displayName` 用于展示中文名称，`open` 用于判断任务是否仍需要处理。把行为放进枚举，比在各处写 `status != DONE` 更集中。

优先级也可以建模成枚举：

```java
public enum StudyTaskPriority {
    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高");
}
```

`weight` 可以用于排序，`displayName` 可以用于摘要文本。

## Objects：必填参数校验

`Objects.requireNonNull` 是常见空值校验工具：

```java
this.status = Objects.requireNonNull(status, "status must not be null");
this.priority = Objects.requireNonNull(priority, "priority must not be null");
```

它会在参数为 `null` 时抛出 `NullPointerException`，并带上你提供的消息。前面课程常用 `IllegalArgumentException` 表达业务输入不合法；这里用 `Objects.requireNonNull` 表达“这个引用必须存在”。

不要把它当成所有校验的替代品。标题为空白仍然需要业务校验：

```java
if (title == null || title.isBlank()) {
    throw new IllegalArgumentException("title must not be blank");
}
```

## Optional：可能没有结果

查询任务时，可能找得到，也可能找不到。返回 `null` 会把风险推给调用者。`Optional<T>` 可以明确表达“可能没有”：

```java
public Optional<StudyTask> findByTitle(String title) {
    String normalizedTitle = normalizeTitle(title);
    return tasks.stream()
            .filter(task -> task.title().equalsIgnoreCase(normalizedTitle))
            .findFirst();
}
```

调用者可以判断：

```java
Optional<StudyTask> task = board.findByTitle("Optional");
task.ifPresent(found -> System.out.println(found.summary()));
```

本章只使用 `Optional` 表达查询结果，不把它作为字段保存。`StudyTask` 内部可以保存一个可空 `dueDate`，对外再返回 `Optional<LocalDate>`：

```java
public Optional<LocalDate> dueDate() {
    return Optional.ofNullable(dueDate);
}
```

## StringJoiner：稳定拼接文本

当摘要由多个部分组成时，`StringJoiner` 比手动处理分隔符更清楚：

```java
StringJoiner joiner = new StringJoiner(" | ");
joiner.add(priority.displayName())
        .add(status.displayName())
        .add(title);
dueDate().ifPresent(date -> joiner.add("due " + date));
return joiner.toString();
```

输出示例：

```text
高 | 待开始 | Stream 复盘 | due 2026-07-05
```

如果任务没有截止日期，最后一段不会出现，也不会留下多余分隔符。

## Comparator：描述排序规则

任务看板需要把开放任务排在一起，先看优先级，再看截止日期，最后看标题：

```java
private static Comparator<StudyTask> taskOrder() {
    return Comparator
            .comparing(StudyTask::priority,
                    Comparator.comparingInt(StudyTaskPriority::weight).reversed())
            .thenComparing(task -> task.dueDate().orElse(LocalDate.MAX))
            .thenComparing(StudyTask::title);
}
```

这段规则可以读成：

1. 按优先级权重降序。
2. 优先级相同时，截止日期更早的排前面。
3. 仍然相同时，按标题排序。

`LocalDate.MAX` 表示“没有截止日期时放到较晚位置”。这比写很多嵌套 `if` 更容易维护。

## EnumMap：按枚举统计

如果键是枚举，`EnumMap` 是专门为枚举键设计的 `Map`：

```java
Map<StudyTaskStatus, Integer> counts = new EnumMap<>(StudyTaskStatus.class);
for (StudyTaskStatus status : StudyTaskStatus.values()) {
    counts.put(status, 0);
}
for (StudyTask task : tasks) {
    counts.merge(task.status(), 1, Integer::sum);
}
```

先为每个状态放入 `0`，可以保证即使某个状态没有任务，摘要里也能显示出来：

```text
待开始=1, 进行中=1, 已完成=1, 受阻=0
```

## 可运行代码

- [`StudyTaskStatus`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyTaskStatus.java) 展示带字段和方法的枚举状态。
- [`StudyTaskPriority`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyTaskPriority.java) 展示带权重的枚举优先级。
- [`StudyTask`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyTask.java) 展示 `Objects`、`Optional` 和 `StringJoiner`。
- [`StudyTaskBoard`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyTaskBoard.java) 展示 `Comparator`、`EnumMap`、查询和排序。
- [`StudyTaskBoardTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/StudyTaskBoardTest.java) 覆盖枚举行为、摘要、排序、查询、统计、只读返回和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 用字符串保存固定状态：拼写错误很难提前发现。
- 在多个地方重复判断枚举状态：可以把简单行为放进枚举方法。
- 滥用 `Optional.get()`：先判断或使用 `ifPresent`、`orElse` 等方法。
- 把 `Optional` 作为所有字段类型：初学阶段优先把它用于返回值。
- 忘记 `Objects.requireNonNull` 抛出的是 `NullPointerException`。
- 手动拼接分隔符：多字段摘要优先考虑 `StringJoiner`。
- 排序规则散落在多个地方：把 `Comparator` 抽成一个清楚的方法。

调试这类代码时，先看枚举常量是否符合业务选项，再看 `Optional` 是否为空，最后看排序规则中每一段比较条件是否符合预期。

## 最佳实践及适用边界

- 固定选项集合优先考虑枚举，而不是字符串常量。
- 枚举可以保存展示名、权重和简单业务判断。
- 查询可能没有结果时，返回 `Optional<T>` 比返回 `null` 更清楚。
- 必填引用可以用 `Objects.requireNonNull` 快速校验。
- 复杂排序用 `Comparator` 组合规则，避免巨大条件分支。
- 枚举键统计可以考虑 `EnumMap`。

## 分级练习

- **基础**：实现 `StudyTaskStatus.open`、`StudyTaskPriority.weight` 和任务摘要。
- **进阶**：实现 `StudyTaskBoard.findByTitle` 和 `firstOverdueTask`，用 `Optional` 表达可能没有结果。
- **挑战**：实现 `openTasksByPriority` 和 `statusSummary`，使用 `Comparator`、`EnumMap` 和 `StringJoiner`。

## 面试与复习题

1. 枚举相比字符串状态有什么优势？
2. 枚举为什么也可以有字段和方法？
3. `Objects.requireNonNull` 适合解决什么问题？
4. `Optional<T>` 表达什么语义？
5. 为什么不建议随手调用 `Optional.get()`？
6. `StringJoiner` 相比手动拼接分隔符有什么好处？
7. `Comparator.thenComparing` 适合什么场景？
8. 什么情况下可以考虑使用 `EnumMap`？

## 本章总结

枚举把固定业务选项变成类型，能减少字符串拼写错误，并把简单行为集中到枚举内部。配合 `Optional`、`Objects`、`StringJoiner`、`Comparator` 和 `EnumMap`，日常 Java 代码可以更明确地表达空值、文本拼接、排序和统计规则。

## 下一步

上一章：[Lambda 与 Stream API](./10-lambda-streams.md)

继续查看：[学习路线](../../roadmap/index.md)

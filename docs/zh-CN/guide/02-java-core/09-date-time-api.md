---
title: 日期与时间 API
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 日期与时间 API

## 学习目标

- 理解 `java.time` 是现代 Java 处理日期时间的首选 API。
- 使用 `LocalDate` 表示日期。
- 使用 `LocalTime` 表示一天中的时间。
- 使用 `LocalDateTime` 表示不带时区的日期时间。
- 使用 `Duration` 表示一段时间长度。
- 使用 `DateTimeFormatter` 输出稳定格式的日历文本。
- 编写测试验证日期计算、时间加法和非法输入。

## 前置知识

已完成[文件 I/O](./08-file-io.md)，能够阅读集合返回、异常校验、文件测试和 `List<String>` 这类类型声明。

## 为什么需要日期与时间 API

课程平台里经常会遇到时间问题：

- 学习计划从哪一天开始。
- 每天几点开始学习。
- 一节学习多久结束。
- 整个计划预计哪一天完成。
- 日历上应该显示什么文本。

不要用字符串或 `long` 毫秒值直接表达这些概念。字符串适合展示，不适合计算；毫秒值适合机器存储，但人很难直接看懂。Java 8 以后推荐使用 `java.time` 包，它把不同时间概念拆成明确类型。

## LocalDate：只表示日期

`LocalDate` 表示年、月、日，不包含几点几分：

```java
LocalDate firstStudyDate = LocalDate.of(2026, 7, 1);
LocalDate secondStudyDate = firstStudyDate.plusDays(1);
```

如果只关心“哪一天”，例如课程开始日期、计划完成日期、生日或账单日期，就优先使用 `LocalDate`。

## LocalTime：只表示一天中的时间

`LocalTime` 表示一天中的时间，不包含日期：

```java
LocalTime dailyStartTime = LocalTime.of(20, 30);
```

如果只关心“每天几点”，例如每天 20:30 学习，就使用 `LocalTime`。它不会告诉你是哪一天的 20:30。

## LocalDateTime：组合日期和时间

当日期和时间都需要时，可以组合成 `LocalDateTime`：

```java
LocalDateTime startsAt = LocalDateTime.of(firstStudyDate, dailyStartTime);
```

本章先使用不带时区的 `LocalDateTime`。它适合表达本地学习计划、日历展示和简单业务时间。跨地区会议、服务器日志和全球用户时间会涉及时区，后续再学习 `ZonedDateTime` 和 `Instant`。

## Duration：表示时间长度

`Duration` 表示一段时间长度，例如 45 分钟：

```java
Duration sessionDuration = Duration.ofMinutes(45);
LocalDateTime endsAt = startsAt.plus(sessionDuration);
```

`Duration` 比裸 `int` 更清楚：`45` 到底是分钟、秒还是小时？写成 `Duration.ofMinutes(45)` 后，单位直接出现在代码里。

`StudySchedule` 构造器会拒绝空值、零时长和负时长：

```java
if (sessionDuration == null || sessionDuration.isZero() || sessionDuration.isNegative()) {
    throw new IllegalArgumentException("session duration must be positive");
}
```

这和前面课程一样：先保护对象不变量，再让业务方法基于可靠状态工作。

## 生成学习日程

`StudySchedule` 保存第一天、每天开始时间和每节学习时长：

```java
private final LocalDate firstStudyDate;
private final LocalTime dailyStartTime;
private final Duration sessionDuration;
private final List<CourseLesson> lessons = new ArrayList<>();
```

第 `n` 节课安排在第一天之后的第 `n - 1` 天：

```java
public LocalDateTime startsAt(int sessionNumber) {
    lessonAt(sessionNumber);
    LocalDate sessionDate = firstStudyDate.plusDays(sessionNumber - 1L);
    return LocalDateTime.of(sessionDate, dailyStartTime);
}
```

结束时间就是开始时间加上本节时长：

```java
public LocalDateTime endsAt(int sessionNumber) {
    return startsAt(sessionNumber).plus(sessionDuration);
}
```

这里没有手写“小时进位”“跨天”等细节，标准库会处理时间计算。

## 计算日期差

`ChronoUnit.DAYS.between` 可以计算两个日期之间相隔多少天：

```java
public long daysUntilStart(LocalDate currentDate) {
    if (currentDate == null) {
        throw new IllegalArgumentException("current date must not be null");
    }
    return ChronoUnit.DAYS.between(currentDate, firstStudyDate);
}
```

如果 `currentDate` 晚于 `firstStudyDate`，结果会是负数。这种返回值有业务含义：计划已经开始了。

## 格式化输出

日期时间对象适合计算，但展示给用户时通常需要固定格式：

```java
private static final DateTimeFormatter CALENDAR_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
```

生成日历行：

```java
return startsAt(sessionNumber).format(CALENDAR_FORMATTER)
        + " - " + lesson.getTitle()
        + " (" + sessionDuration.toMinutes() + " min)";
```

输出示例：

```text
2026-07-01 19:00 - 日期与时间 API (50 min)
```

格式化应该放在靠近展示边界的位置。对象内部计算仍然使用 `LocalDate`、`LocalTime`、`LocalDateTime` 和 `Duration`。

## 测试日期时间代码

日期时间测试不要依赖“现在”。如果测试里直接使用当前系统时间，明天、下个月或换一台机器运行时可能变得不稳定。

更可靠的做法是使用固定日期：

```java
StudySchedule schedule = new StudySchedule(
        LocalDate.of(2026, 6, 26),
        LocalTime.of(20, 30),
        Duration.ofMinutes(45));
```

然后验证确定结果：

```java
assertEquals(LocalDateTime.of(2026, 6, 26, 20, 30), schedule.startsAt(1));
assertEquals(LocalDateTime.of(2026, 6, 26, 21, 15), schedule.endsAt(1));
assertEquals(LocalDate.of(2026, 6, 27), schedule.completionDate());
```

这种测试读起来像一张小日历，也能稳定重复运行。

## 可运行代码

- [`StudySchedule`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudySchedule.java) 展示 `LocalDate`、`LocalTime`、`LocalDateTime`、`Duration`、`ChronoUnit` 和 `DateTimeFormatter`。
- [`StudyScheduleTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/StudyScheduleTest.java) 覆盖日期递增、结束时间、完成日期、格式化、只读返回和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 用字符串做日期计算：字符串适合展示，不适合加减日期。
- 把所有时间都存成 `long`：单位不清楚，业务含义也不清楚。
- 忘记 `LocalDateTime` 不带时区：跨地区或服务器时间场景需要更明确的类型。
- 用系统当前时间写测试：测试结果可能随日期变化。
- 手写时间进位逻辑：优先让 `java.time` 处理加减。
- 混淆 `Duration` 和课程原本的“预计学习分钟数”：前者是时间长度类型，后者只是业务估算字段。

调试日期时间代码时，先确认变量类型，再看输入值和加减单位。大多数问题都来自“本来只需要日期，却混进了时间”或“本来需要时长，却用了普通数字”。

## 最佳实践及适用边界

- 只需要日期时使用 `LocalDate`。
- 只需要一天中的时间时使用 `LocalTime`。
- 需要本地日期和时间时使用 `LocalDateTime`。
- 需要表示时间长度时使用 `Duration`。
- 展示前再格式化，计算时保留日期时间对象。
- 测试中优先使用固定日期时间，不依赖系统当前时间。
- 本章暂不处理时区；跨地区和服务端统一时间后续再使用 `ZonedDateTime`、`OffsetDateTime` 或 `Instant`。

## 分级练习

- **基础**：实现 `StudySchedule.addLesson`、`startsAt` 和 `endsAt`。
- **进阶**：实现 `completionDate`、`daysUntilStart` 和只读 `lessons` 返回。
- **挑战**：实现 `calendarLine`，用 `DateTimeFormatter` 输出稳定日历文本，并测试非法课程序号。

## 面试与复习题

1. 为什么不建议用字符串直接做日期时间计算？
2. `LocalDate` 和 `LocalTime` 分别表达什么？
3. `LocalDateTime` 为什么不等于“全球唯一时间点”？
4. `Duration` 比普通数字更清楚在哪里？
5. `plusDays` 和 `plus(Duration)` 分别适合什么对象？
6. 为什么日期时间测试要尽量使用固定值？
7. `DateTimeFormatter` 应该放在计算核心还是展示边界？
8. 什么场景需要进一步学习带时区的时间类型？

## 本章总结

`java.time` 用明确类型表达不同时间概念。`LocalDate` 管日期，`LocalTime` 管一天中的时间，`LocalDateTime` 组合两者，`Duration` 表示时长。用这些类型建模后，代码更容易计算、测试和维护。

## 下一步

上一章：[文件 I/O](./08-file-io.md)

继续查看：[学习路线](../../roadmap/index.md)

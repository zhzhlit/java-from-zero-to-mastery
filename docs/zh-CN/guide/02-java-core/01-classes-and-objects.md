---
title: 类与对象
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 类与对象

## 学习目标

- 区分类、对象、字段、构造器和方法。
- 使用 `private` 字段和公开方法保护对象状态。
- 编写带校验的构造器，避免创建无效对象。
- 理解实例方法如何读取和修改同一个对象的状态。
- 让一个对象持有另一个对象引用，表达简单协作关系。

## 前置知识

已完成[IntelliJ IDEA 调试](../01-getting-started/08-debugging-with-intellij-idea.md)，能够阅读测试失败信息，并能解释方法参数、返回值和字符串比较。

## 为什么需要类

前面课程主要使用静态方法处理一段输入。面向对象开始关注“把数据和行为放在同一个概念里”。

一个课时不仅有标题和时长，还需要保证标题不能为空、时长必须为正数。把这些规则散落在多个方法中很容易遗漏；类可以把状态和规则集中起来：

```java
CourseLesson lesson = new CourseLesson("类与对象", 45);
System.out.println(lesson.toDisplayText());
```

`CourseLesson` 是类，`lesson` 是这个类创建出来的对象。

## 字段与构造器

字段保存对象状态。构造器负责创建对象时初始化字段：

```java
public class CourseLesson {

    private final String title;
    private final int durationMinutes;

    public CourseLesson(String title, int durationMinutes) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration minutes must be positive");
        }
        this.title = title.strip();
        this.durationMinutes = durationMinutes;
    }
}
```

`this.title` 表示当前对象的字段，右侧的 `title` 是构造器参数。字段使用 `private`，调用者不能绕过构造器直接写入无效状态。

## getter 与实例方法

对象通常通过公开方法暴露必要信息：

```java
public String getTitle() {
    return title;
}

public String toDisplayText() {
    return title + " (" + durationMinutes + " min)";
}
```

`getTitle` 和 `toDisplayText` 都是实例方法。调用时必须先有对象：

```java
CourseLesson lesson = new CourseLesson("封装", 40);
String title = lesson.getTitle();
```

实例方法可以读取当前对象字段；如果字段不是 `final`，实例方法也可以按规则修改它。

## 可变对象状态

学习进度会随时间变化，因此 `completedMinutes` 不是 `final`：

```java
public class LessonProgress {

    private final CourseLesson lesson;
    private int completedMinutes;

    public void recordStudyMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes must be positive");
        }
        completedMinutes = Math.min(
                lesson.getDurationMinutes(),
                completedMinutes + minutes);
    }
}
```

这里仍然不允许调用者直接修改字段。调用者只能通过 `recordStudyMinutes` 增加学习时长，因此对象可以保证完成分钟数不会超过课时时长。

## 对象之间的协作

`LessonProgress` 持有一个 `CourseLesson` 对象引用：

```java
CourseLesson lesson = new CourseLesson("构造器", 30);
LessonProgress progress = new LessonProgress(lesson);
```

进度对象不需要复制课时标题和时长，只要引用课时对象并在需要时调用它的方法：

```java
public int remainingMinutes() {
    return lesson.getDurationMinutes() - completedMinutes;
}
```

这就是最基础的对象协作：一个对象负责课时信息，另一个对象负责学习进度。

## static 与实例成员

前面章节的工具类常写成：

```java
public static int sum(int[] values)
```

静态方法属于类本身，不依赖某个对象。实例方法属于具体对象，可以访问该对象的字段。

本章优先使用实例方法，因为每个课时、每份学习进度都有自己的状态。若方法只是无状态工具，再考虑 `static`。

## 可运行代码

- [`CourseLesson`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/CourseLesson.java) 展示字段、构造器、getter 和实例方法。
- [`LessonProgress`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/LessonProgress.java) 展示可变状态和对象协作。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供课程进度 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 字段设为 `public`：外部代码可以绕过规则写入无效状态。
- 构造器不校验参数：对象一创建就可能处于错误状态。
- 忘记 `this`：字段名和参数名相同时，要确认赋值方向。
- 把所有方法都写成 `static`：如果方法需要读取或修改对象状态，应使用实例方法。
- 可变字段没有边界：修改状态的方法要防止负数、越界或重复扣减。

调试对象状态时，在构造器赋值行和修改字段的方法中设置断点，观察 `this`、参数和字段变化。

## 最佳实践及适用边界

- 字段优先使用 `private`，通过方法表达允许的操作。
- 创建对象时就完成必要校验，避免“半合法对象”在系统中流动。
- 不会变化的字段使用 `final`，会变化的字段只能通过受控方法修改。
- 方法名使用业务动作，例如 `recordStudyMinutes`，比 `setCompletedMinutes` 更能表达规则。
- 初学阶段先掌握封装和对象协作，继承、多态和接口放到后续课程展开。

## 分级练习

- **基础**：为 `Lesson` 增加标题和时长校验。
- **进阶**：实现 `CourseProgress.recordStudyMinutes`，确保完成分钟数不会超过课时时长。
- **挑战**：实现固定格式 `summary`，并为 0%、75%、100% 三种进度补充测试。

## 面试与复习题

1. 类和对象有什么区别？
2. 字段为什么通常不设为 `public`？
3. 构造器的职责是什么？
4. `this` 在构造器中常用于解决什么问题？
5. getter 和直接公开字段有什么区别？
6. 静态方法和实例方法有什么区别？
7. 为什么 `completedMinutes` 不适合声明为 `final`？
8. 一个对象持有另一个对象引用时，双方分别负责什么状态？

## 本章总结

类把一组数据和相关行为组织成一个明确概念。通过私有字段、构造器校验和实例方法，代码可以把对象状态控制在合法范围内，并用对象协作表达真实业务关系。

## 下一步

上一章：[IntelliJ IDEA 调试](../01-getting-started/08-debugging-with-intellij-idea.md)

继续查看：[学习路线](../../roadmap/index.md)

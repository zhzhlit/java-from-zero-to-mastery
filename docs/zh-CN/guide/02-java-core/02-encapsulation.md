---
title: 封装
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 封装

## 学习目标

- 解释封装为什么不只是把字段改成 `private`。
- 识别对象不变量，并让构造器和实例方法共同维护它。
- 用业务方法替代盲目的 setter。
- 区分查询方法和修改状态的方法。
- 编写测试覆盖合法修改、非法输入和边界变化。

## 前置知识

已完成[类与对象](./01-classes-and-objects.md)，能够编写字段、构造器、getter 和实例方法，并理解对象状态会随方法调用变化。

## 封装解决什么问题

上一章已经使用 `private` 字段防止调用者直接写对象状态。封装更进一步：对象不仅隐藏字段，还要把“状态如何变化”的规则集中在自己内部。

以学习目标为例，它有三个状态：

- `title`：目标名称，不能为空。
- `targetMinutes`：目标分钟数，必须大于 0。
- `completedMinutes`：已完成分钟数，不能小于 0，也不能超过目标分钟数。

最后一条就是对象不变量。只要对象存在，就应该始终满足它。

## 不变量

如果把字段公开，调用者可以轻易写出矛盾状态：

```java
goal.completedMinutes = 80;
goal.targetMinutes = 30;
```

此时对象既说目标是 30 分钟，又说已经完成 80 分钟。后续任何百分比、剩余分钟数或完成状态都可能出错。

封装的做法是让字段保持私有，并让对象自己决定哪些修改合法：

```java
public class StudyGoal {

    private String title;
    private int targetMinutes;
    private int completedMinutes;

    public StudyGoal(String title, int targetMinutes) {
        this.title = normalizeTitle(title);
        if (targetMinutes <= 0) {
            throw new IllegalArgumentException("target minutes must be positive");
        }
        this.targetMinutes = targetMinutes;
    }
}
```

构造器保证对象从创建开始就是合法的。

## 用业务方法修改状态

初学者常把所有字段都配上 `setXxx`。这样虽然字段仍是 `private`，但调用者仍然可以绕过业务规则。

更好的做法是提供有意义的业务动作：

```java
public void recordMinutes(int minutes) {
    if (minutes <= 0) {
        throw new IllegalArgumentException("minutes must be positive");
    }
    completedMinutes = Math.min(targetMinutes, completedMinutes + minutes);
}
```

调用者只能记录一次学习时长，不能直接把完成分钟数改成任意值。对象内部使用 `Math.min` 保证完成分钟数不会超过目标。

修改目标分钟数也要维护同一个不变量：

```java
public void changeTargetMinutes(int targetMinutes) {
    if (targetMinutes <= 0) {
        throw new IllegalArgumentException("target minutes must be positive");
    }
    this.targetMinutes = targetMinutes;
    completedMinutes = Math.min(completedMinutes, targetMinutes);
}
```

如果原本完成 45 分钟，后来把目标改成 30 分钟，对象会同步把已完成分钟数压到 30。这样 `completedMinutes <= targetMinutes` 始终成立。

## 查询方法

查询方法不修改状态，只返回当前对象能公开的信息：

```java
public int remainingMinutes() {
    return targetMinutes - completedMinutes;
}

public double completionRate() {
    return (double) completedMinutes / targetMinutes;
}

public boolean isAchieved() {
    return completedMinutes == targetMinutes;
}
```

调用者不需要知道字段如何保存，只需要调用能表达业务含义的方法。

## 校验逻辑复用

当构造器和普通方法都需要校验标题时，可以提取一个私有静态方法：

```java
private static String normalizeTitle(String title) {
    if (title == null || title.isBlank()) {
        throw new IllegalArgumentException("title must not be blank");
    }
    return title.strip();
}
```

这个方法只服务于类内部，所以使用 `private`。封装不仅限制字段访问，也限制辅助方法的可见范围。

## 可运行代码

- [`StudyGoal`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyGoal.java) 展示不变量、业务方法和查询方法。
- [`StudyGoalTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/StudyGoalTest.java) 覆盖构造、记录学习、修改目标和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 只写 `private` 字段，却提供任意 setter：调用者仍然能制造无效状态。
- 构造器校验了输入，普通修改方法却没有校验：对象后续仍可能被改坏。
- 修改一个字段时忘记同步相关字段：例如降低目标分钟数后，已完成分钟数仍超过目标。
- 把查询方法写成修改状态的方法：`completionRate` 这样的查询不应该改变对象。
- 为了测试方便公开字段：测试应该通过公开行为观察对象，而不是绕过封装。

调试封装问题时，在构造器和每个修改状态的方法里设置断点，重点观察方法调用前后的不变量是否仍然成立。

## 最佳实践及适用边界

- 先写出对象必须长期满足的不变量，再设计构造器和修改方法。
- 字段默认 `private`，只有真正需要被外部读取的信息才提供 getter。
- 修改状态的方法使用业务动作命名，例如 `recordMinutes`、`changeTargetMinutes`、`rename`。
- 简单对象不必为了“完整”生成所有 getter/setter，公开 API 越少越容易维护。
- 封装不能替代清晰需求；如果规则本身不明确，先用测试和文档把规则写清楚。

## 分级练习

- **基础**：在 `StudyGoal` 中校验标题和目标分钟数。
- **进阶**：实现 `recordMinutes` 和 `changeTargetMinutes`，维护完成分钟数不超过目标。
- **挑战**：实现 `summary`，并补充 0%、75%、100% 三种进度测试。

## 面试与复习题

1. 封装和 `private` 字段有什么区别？
2. 什么是不变量？为什么它对对象设计重要？
3. 为什么盲目生成 setter 可能破坏封装？
4. `recordMinutes` 比 `setCompletedMinutes` 多表达了哪些规则？
5. 修改目标分钟数时，为什么要同步处理已完成分钟数？
6. 查询方法和修改状态的方法有什么区别？
7. 私有辅助方法适合解决什么问题？
8. 你会如何测试一个对象始终保持合法状态？

## 本章总结

封装的核心是让对象自己守住状态规则。私有字段只是开始，真正重要的是构造器、业务方法和查询方法一起维护对象不变量，让外部代码只能通过清晰、受控的行为使用对象。

## 下一步

上一章：[类与对象](./01-classes-and-objects.md)

继续查看：[学习路线](../../roadmap/index.md)

---
title: 异常处理
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 异常处理

## 学习目标

- 理解异常表示程序无法按当前路径继续执行。
- 使用 `throw` 主动报告非法输入或业务失败。
- 使用 `try/catch` 在合适边界处理预期失败。
- 编写自定义异常表达业务语义。
- 编写测试覆盖成功路径、异常路径和失败后状态。

## 前置知识

已完成[接口](./04-interfaces.md)，能够阅读构造器校验、业务方法、接口引用和测试断言。

## 为什么需要异常

前面课程已经多次使用 `IllegalArgumentException` 拒绝无效参数。异常的作用是：当方法无法完成承诺时，立刻中断当前执行路径，并把失败原因交给调用者。

课程报名就是一个典型场景：

- 课程不能为空。
- 容量必须大于 0。
- 学员姓名不能为空。
- 名额满了以后，不能继续报名。

前三类是调用者给了非法参数，适合抛 `IllegalArgumentException`。名额满了是业务失败，适合用一个更具体的异常表达。

## 自定义异常

自定义异常可以让失败原因更贴近业务语言：

```java
public class EnrollmentException extends RuntimeException {

    public EnrollmentException(String message) {
        super(message);
    }
}
```

这里继承 `RuntimeException`，表示调用者可以选择捕获，也可以让异常继续向外传播。初学阶段先把重点放在“哪里抛、哪里处理、测试如何覆盖”。

## 抛出异常

`EnrollmentService` 在构造器里校验基础状态：

```java
public EnrollmentService(CourseLesson lesson, int capacity) {
    if (lesson == null) {
        throw new IllegalArgumentException("lesson must not be null");
    }
    if (capacity <= 0) {
        throw new IllegalArgumentException("capacity must be positive");
    }
    this.lesson = lesson;
    this.capacity = capacity;
}
```

报名时先校验姓名，再检查容量：

```java
private String enrollAndReturnName(String studentName) {
    String normalizedName = normalizeStudentName(studentName);
    if (enrolledCount >= capacity) {
        throw new EnrollmentException("course is full: " + lesson.getTitle());
    }
    enrolledCount++;
    return normalizedName;
}
```

注意顺序：只有所有校验通过，才修改 `enrolledCount`。这样异常发生后，对象不会进入半更新状态。

## 捕获异常

不是每个方法都应该捕获异常。`enroll` 直接抛出异常，让调用者知道报名失败：

```java
public void enroll(String studentName) {
    enrollAndReturnName(studentName);
}
```

如果某个边界需要把失败转换成提示文本，可以在这里捕获：

```java
public String tryEnroll(String studentName) {
    try {
        String normalizedName = enrollAndReturnName(studentName);
        return normalizedName + " enrolled in " + lesson.getTitle();
    } catch (IllegalArgumentException | EnrollmentException exception) {
        return "Enrollment failed: " + exception.getMessage();
    }
}
```

这里的 `catch` 只处理预期失败：非法输入和名额已满。它不会吞掉所有异常，也不会假装报名成功。

## 测试异常路径

JUnit 5 使用 `assertThrows` 验证异常：

```java
EnrollmentException exception = assertThrows(EnrollmentException.class,
        () -> service.enroll("Grace"));

assertEquals("course is full: 异常处理", exception.getMessage());
assertEquals(1, service.getEnrolledCount());
```

最后一行很重要：它确认失败后报名人数没有增加。异常测试不只看“有没有抛”，还要看对象状态是否仍然正确。

## 可运行代码

- [`EnrollmentException`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/EnrollmentException.java) 展示自定义业务异常。
- [`EnrollmentService`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/EnrollmentService.java) 展示抛出异常和捕获预期失败。
- [`EnrollmentServiceTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/EnrollmentServiceTest.java) 覆盖成功报名、容量满、非法输入和失败后状态。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 用返回值 `null` 表示所有失败：调用者很难知道失败原因。
- 到处写 `try/catch`：捕获异常的位置应该真的知道如何处理失败。
- 捕获 `Exception` 后什么都不做：这会隐藏问题，让后续错误更难定位。
- 修改状态后再抛异常：对象可能留下半更新状态。
- 测试只验证异常类型，不验证消息和状态：边界行为仍可能有漏洞。

调试异常时，在 `throw` 所在行打断点，观察异常抛出前对象状态；也可以在 `catch` 块打断点，确认捕获的是预期异常。

## 最佳实践及适用边界

- 参数无效时使用 `IllegalArgumentException`，业务失败可以使用更具体的自定义异常。
- 先校验，再修改对象状态。
- 只在能恢复、转换或记录失败的边界捕获异常。
- 不要把异常当作普通流程控制工具；能用简单判断表达的分支，不必强行抛异常。
- 测试异常路径时，同时验证异常消息和对象状态。

## 分级练习

- **基础**：补全 `EnrollmentService` 的构造器和学员姓名校验。
- **进阶**：实现容量检查，满员时抛出 `EnrollmentException`。
- **挑战**：实现 `tryEnroll`，把预期异常转换成稳定提示，并测试失败后报名人数不变。

## 面试与复习题

1. 异常和普通返回值分别适合表达什么情况？
2. `throw` 会如何影响当前方法的执行？
3. 为什么构造器里也常见参数校验？
4. 自定义异常有什么价值？
5. 为什么不建议捕获所有 `Exception` 后忽略？
6. 异常发生前后，为什么要关注对象状态？
7. `try/catch` 应该放在什么样的代码边界？
8. 你会如何测试一个业务异常？

## 本章总结

异常用于表达方法无法按约定完成的失败。好的异常处理会在对象内部及时拒绝非法状态，在业务边界清晰处理预期失败，并通过测试确认失败不会破坏对象状态。

## 下一步

上一章：[接口](./04-interfaces.md)

继续查看：[学习路线](../../roadmap/index.md)

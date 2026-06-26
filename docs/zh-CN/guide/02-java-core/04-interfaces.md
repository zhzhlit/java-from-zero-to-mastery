---
title: 接口
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 接口

## 学习目标

- 解释接口适合表达什么样的能力契约。
- 使用 `interface` 定义方法约定和默认方法。
- 使用 `implements` 让不同类遵守同一个接口。
- 用接口引用处理不同实现类对象。
- 区分接口和抽象类的适用场景。

## 前置知识

已完成[继承与多态](./03-inheritance-and-polymorphism.md)，能够理解父类引用、多态调用和 `@Override`。

## 为什么需要接口

继承适合表达“是一个”关系。接口更适合表达“能做什么”。

测验结果和代码评审结果不是同一种父子关系：一个来自选择题，一个来自自动检查。但它们都能提供标题、得分、满分、是否通过和报告行。这个共同能力可以用接口表达。

## 定义接口

接口使用 `interface` 声明：

```java
public interface ReviewResult {

    String title();

    int score();

    int maxScore();
}
```

接口里的抽象方法没有方法体。实现类必须提供这些方法，否则编译不能通过。

接口描述的是调用者可以依赖的契约：只要一个对象是 `ReviewResult`，调用者就可以读取标题、得分和满分。

## 默认方法

接口也可以提供默认方法，复用基于契约能推导出的通用行为：

```java
default double scoreRate() {
    return (double) score() / maxScore();
}

default boolean passed() {
    return scoreRate() >= 0.6;
}

default String reportLine() {
    return title() + ": " + score() + "/" + maxScore()
            + " - " + (passed() ? "PASS" : "RETRY");
}
```

这些方法依赖 `title`、`score` 和 `maxScore`，但不需要知道具体对象是测验结果还是代码评审结果。

## 实现接口

测验结果使用 `implements` 实现接口：

```java
public class QuizResult implements ReviewResult {

    private final String title;
    private final int correctAnswers;
    private final int totalQuestions;

    @Override
    public String title() {
        return title;
    }

    @Override
    public int score() {
        return correctAnswers;
    }

    @Override
    public int maxScore() {
        return totalQuestions;
    }
}
```

代码评审结果可以有完全不同的字段，但只要实现同一组方法，也能遵守同一个接口：

```java
public class CodeReviewResult implements ReviewResult {

    private final String title;
    private final int passedChecks;
    private final int totalChecks;

    @Override
    public String title() {
        return title;
    }

    @Override
    public int score() {
        return passedChecks;
    }

    @Override
    public int maxScore() {
        return totalChecks;
    }
}
```

`@Override` 同样适用于接口实现方法，能让编译器检查方法签名是否正确。

## 接口多态

接口引用可以指向任意实现类对象：

```java
ReviewResult[] results = {
        new QuizResult("接口基础", 9, 10),
        new CodeReviewResult("实现练习", 4, 5)
};
```

调用者只面向接口编程：

```java
int passedCount = 0;
for (ReviewResult result : results) {
    if (result.passed()) {
        passedCount++;
    }
    System.out.println(result.reportLine());
}
```

这里不需要关心具体实现类。新增一种结果类型时，只要实现 `ReviewResult`，这段调用代码仍然可以工作。

## 接口与抽象类

上一章的 `LearningResource` 抽象类保存了共同字段 `title`，并定义了统一摘要流程。抽象类适合共享状态和稳定流程。

接口通常不保存对象状态，它更适合定义能力边界。一个类只能继承一个父类，但可以实现多个接口。初学阶段先记住：

- 有共同状态和稳定父子关系时，考虑抽象类。
- 只想表达“这个类能提供某种能力”时，优先考虑接口。

## 可运行代码

- [`ReviewResult`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/ReviewResult.java) 展示接口、抽象方法和默认方法。
- [`QuizResult`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/QuizResult.java) 展示测验结果实现。
- [`CodeReviewResult`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/CodeReviewResult.java) 展示代码评审结果实现。
- [`ReviewResultTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/ReviewResultTest.java) 覆盖默认方法、接口引用和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 把接口当成共享字段的地方：接口更适合定义能力，不适合保存每个对象的状态。
- 实现类漏掉接口方法：编译器会提示类必须实现抽象方法。
- 方法签名写错：加上 `@Override` 可以更快定位问题。
- 默认方法访问不存在的字段：接口默认方法只能依赖接口方法或常量。
- 为了接口而接口：如果只有一个实现类且没有明确替换需求，可以先保持普通类。

调试接口多态时，在接口默认方法和不同实现类的 `score`、`maxScore` 方法中设置断点，观察运行时进入了哪个实现。

## 最佳实践及适用边界

- 接口名优先表达能力或契约，例如 `ReviewResult`、`Runnable`、`Comparable`。
- 接口方法保持少而清晰，调用者越容易理解越好。
- 默认方法适合复用基于接口方法能推导出的行为。
- 实现类仍然负责自己的字段校验和不变量维护。
- 当接口开始变得庞大时，考虑拆成多个更小的接口。

## 分级练习

- **基础**：补全 `ReviewResult` 的默认方法。
- **进阶**：实现 `QuizResult` 和 `CodeReviewResult` 的字段校验与接口方法。
- **挑战**：使用 `ReviewResult[]` 汇总不同实现类的通过数量和报告文本。

## 面试与复习题

1. 接口适合表达什么？
2. `implements` 和 `extends` 有什么区别？
3. 接口默认方法可以解决什么问题？
4. 为什么实现接口的方法也建议写 `@Override`？
5. 接口引用为什么可以指向不同实现类对象？
6. 接口和抽象类各自适合什么场景？
7. 默认方法为什么不应该依赖实现类字段？
8. 什么时候不必急着抽接口？

## 本章总结

接口定义一组调用者可以依赖的能力契约。不同类可以通过 `implements` 遵守同一个接口，并借助接口引用实现多态调用。和抽象类相比，接口更关注“能做什么”，而不是“共同状态放在哪里”。

## 下一步

上一章：[继承与多态](./03-inheritance-and-polymorphism.md)

下一章：[异常处理](./05-exceptions.md)

继续查看：[学习路线](../../roadmap/index.md)

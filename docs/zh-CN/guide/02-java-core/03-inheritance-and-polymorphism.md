---
title: 继承与多态
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 继承与多态

## 学习目标

- 解释继承适合表达什么样的“是一个”关系。
- 使用抽象父类提取共同字段和通用方法。
- 使用 `extends`、`super` 和 `@Override` 编写子类。
- 理解父类引用如何调用子类的重写方法。
- 识别继承的适用边界，避免为了复用而滥用继承。

## 前置知识

已完成[封装](./02-encapsulation.md)，能够通过私有字段、构造器校验和业务方法维护对象状态。

## 为什么需要继承

课程平台里可能有多种学习资源：视频、文章、测验、代码实验。它们都有标题，也都能给出学习摘要；但每种资源估算学习时间的方式不同。

如果每个类都重复写标题校验和摘要拼接，规则会散落到多个地方。继承可以把共同部分放进父类，把差异留给子类。

## 抽象父类

父类 `LearningResource` 保存共同字段，并定义统一的公开行为：

```java
public abstract class LearningResource {

    private final String title;

    protected LearningResource(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.title = title.strip();
    }

    public final String getTitle() {
        return title;
    }

    public final String summary() {
        return typeLabel() + ": " + title + " - " + detailText();
    }

    public abstract int estimatedMinutes();

    protected abstract String typeLabel();

    protected abstract String detailText();
}
```

`abstract` 表示这个类只描述共同概念，不能直接创建 `new LearningResource(...)`。它要求子类补充 `estimatedMinutes`、`typeLabel` 和 `detailText`。

`summary` 声明为 `final`，表示子类不能改写摘要结构，只能通过重写受保护的细节方法提供差异。

## 子类与 super

视频资源是学习资源的一种，因此使用 `extends`：

```java
public class VideoResource extends LearningResource {

    private final int durationMinutes;
    private final boolean hasCaptions;

    public VideoResource(String title, int durationMinutes, boolean hasCaptions) {
        super(title);
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("duration minutes must be positive");
        }
        this.durationMinutes = durationMinutes;
        this.hasCaptions = hasCaptions;
    }
}
```

`super(title)` 调用父类构造器，让父类负责标题校验和保存。子类只处理自己新增的字段。

文章资源也继承同一个父类，但拥有不同字段：

```java
public class ArticleResource extends LearningResource {

    private final int wordCount;

    public ArticleResource(String title, int wordCount) {
        super(title);
        if (wordCount <= 0) {
            throw new IllegalArgumentException("word count must be positive");
        }
        this.wordCount = wordCount;
    }
}
```

## 重写方法

子类使用 `@Override` 明确重写父类定义的方法：

```java
@Override
public int estimatedMinutes() {
    return durationMinutes;
}
```

文章的估算方式不同：

```java
@Override
public int estimatedMinutes() {
    return Math.max(1, (int) Math.ceil(wordCount / 250.0));
}
```

同一个方法名，在不同子类里有不同实现，这为多态打下基础。

## 多态

多态允许父类引用指向不同子类对象：

```java
LearningResource[] resources = {
        new VideoResource("继承视频", 15, false),
        new ArticleResource("多态文章", 500)
};
```

遍历时，变量类型是 `LearningResource`，但实际调用的是每个对象所属子类的方法：

```java
int totalMinutes = 0;
for (LearningResource resource : resources) {
    totalMinutes += resource.estimatedMinutes();
    System.out.println(resource.summary());
}
```

视频返回自己的时长，文章按字数估算时长。调用者不需要写 `if` 判断资源类型，这就是多态带来的扩展能力。

## 可运行代码

- [`LearningResource`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/LearningResource.java) 展示抽象父类和模板方法。
- [`VideoResource`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/VideoResource.java) 展示视频子类。
- [`ArticleResource`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/ArticleResource.java) 展示文章子类。
- [`LearningResourceTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/LearningResourceTest.java) 覆盖继承、重写和父类引用多态调用。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 把“有一个”关系写成继承：课程有学习资源，不代表课程是学习资源。
- 子类构造器忘记调用 `super(...)`：父类状态没有机会初始化。
- 重写方法签名写错：加上 `@Override` 可以让编译器及时发现。
- 为了复用一两行代码强行继承：继承会带来长期结构约束，不只是代码复用手段。
- 父类暴露太多可变方法：子类越多，父类 API 越难修改。

调试多态时，在父类 `summary` 和各个子类的 `estimatedMinutes`、`detailText` 中设置断点，观察同一个父类变量在运行时进入了哪个子类方法。

## 最佳实践及适用边界

- 只有存在稳定“是一个”关系时才考虑继承。
- 父类放共同状态和稳定流程，子类只补充差异行为。
- `@Override` 是重写方法的好习惯，能帮助编译器检查意图。
- 父类方法能保持稳定时再声明为 `final`，避免子类破坏统一规则。
- 如果只是想定义能力，而不是共享状态，后续学习接口会更适合。

## 分级练习

- **基础**：补全 `LearningResource` 的标题校验和统一 `summary`。
- **进阶**：实现 `VideoResource` 和 `ArticleResource` 的字段校验与重写方法。
- **挑战**：用 `LearningResource[]` 保存不同子类对象，并计算总学习时长。

## 面试与复习题

1. 继承适合表达什么关系？
2. 抽象类和普通类有什么不同？
3. 子类构造器中的 `super(...)` 有什么作用？
4. `@Override` 能帮你发现什么错误？
5. 为什么父类引用可以指向子类对象？
6. 多态如何减少调用者的类型判断？
7. 什么时候不应该使用继承？
8. `final` 方法在父类中有什么意义？

## 本章总结

继承把共同状态和稳定流程放到父类，子类通过重写方法表达差异。多态让调用者用同一种父类视角处理不同子类对象，从而减少类型判断，让新增资源类型更容易接入。

## 下一步

上一章：[封装](./02-encapsulation.md)

下一章：[接口](./04-interfaces.md)

继续查看：[学习路线](../../roadmap/index.md)

---
title: 泛型
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 泛型

## 学习目标

- 理解泛型用于把“类型”变成可复用参数。
- 使用泛型类保存指定类型的数据。
- 使用泛型方法在调用时保留具体返回类型。
- 使用上界约束类型参数能调用的能力。
- 避免裸类型和不必要的强制类型转换。
- 编写测试验证类型安全、空值校验和边界行为。

## 前置知识

已完成[集合](./06-collections.md)，能够阅读 `List<CourseLesson>`、`Set<String>`、`Map<String, List<CourseLesson>>`，并理解集合元素类型的作用。

## 为什么需要泛型

集合章节已经用过很多尖括号：

```java
List<CourseLesson> lessons = new ArrayList<>();
```

这里的 `CourseLesson` 就是类型参数。它告诉编译器：这个列表只保存 `CourseLesson`。取出元素时，编译器也知道结果是 `CourseLesson`，不需要手动强制转换。

泛型的核心价值是：写一份逻辑，让它在不同类型上安全复用。

如果没有泛型，队列可能只能保存 `Object`：

```java
Object item = queue.completeNext();
CourseLesson lesson = (CourseLesson) item;
```

这种写法容易把错误拖到运行时。泛型把类型检查提前到编译期，让错误更早、更清楚。

## 泛型类

`LearningQueue<T>` 是一个泛型类。`T` 是类型参数，表示队列里元素的类型：

```java
public class LearningQueue<T> {

    private final List<T> items = new ArrayList<>();

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        items.add(item);
    }
}
```

在类内部，`T` 可以像普通类型一样使用：

- `List<T>` 表示保存 `T` 元素的列表。
- `add(T item)` 表示只能添加 `T` 类型的对象。
- `T completeNext()` 表示返回值仍然是 `T`。

## 使用泛型类

创建队列时写清楚具体类型：

```java
LearningQueue<CourseLesson> lessons = new LearningQueue<>();
lessons.add(new CourseLesson("集合", 40));

CourseLesson nextLesson = lessons.completeNext();
```

右侧 `new LearningQueue<>()` 使用了菱形语法，编译器会根据左侧的 `LearningQueue<CourseLesson>` 推断类型。

同一个泛型类也能保存评审结果：

```java
LearningQueue<ReviewResult> results = new LearningQueue<>();
results.add(new QuizResult("泛型基础", 8, 10));
results.add(new CodeReviewResult("泛型练习", 5, 5));
```

这就是泛型的复用：队列逻辑只写一次，但元素类型由调用者决定。

## 类型安全

如果变量声明为 `LearningQueue<CourseLesson>`，编译器就不允许加入评审结果：

```java
LearningQueue<CourseLesson> lessons = new LearningQueue<>();
lessons.add(new QuizResult("泛型基础", 8, 10)); // 编译错误
```

这个错误在运行前就能发现。泛型不是为了让代码看起来复杂，而是为了让类型边界写在代码里，让编译器帮你守住边界。

## 泛型方法

类型参数也可以写在方法上。`ReviewResultSelector.bestResult` 会从一组评审结果中选出得分率最高的一项：

```java
public static <T extends ReviewResult> T bestResult(List<T> results) {
    if (results == null || results.isEmpty()) {
        throw new IllegalArgumentException("results must not be empty");
    }

    T best = requireResult(results.get(0));
    for (int index = 1; index < results.size(); index++) {
        T current = requireResult(results.get(index));
        if (current.scoreRate() > best.scoreRate()) {
            best = current;
        }
    }
    return best;
}
```

方法返回值是 `T`，所以传入 `List<QuizResult>` 时，返回值就是 `QuizResult`：

```java
QuizResult best = ReviewResultSelector.bestResult(quizzes);
```

如果方法只返回 `ReviewResult`，调用者就会丢失具体类型。泛型方法可以在复用逻辑的同时保留具体类型。

## 上界

`<T extends ReviewResult>` 表示：`T` 必须是 `ReviewResult` 或它的实现类。

这个约束很重要。因为只有确认 `T` 具备 `ReviewResult` 的能力，方法里才能安全调用：

```java
current.scoreRate()
best.scoreRate()
```

如果没有 `extends ReviewResult`，编译器只知道 `T` 是某种未知类型，不能保证它有 `scoreRate` 方法。

## 裸类型

不要这样写：

```java
LearningQueue queue = new LearningQueue();
```

这种写法叫裸类型。它绕开了泛型检查，队列里可以混入不同类型，取出元素时又需要强制转换。初学阶段只要记住：声明集合或泛型类时，尽量写完整类型参数。

## 可运行代码

- [`LearningQueue`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/LearningQueue.java) 展示泛型类、类型参数和只读返回。
- [`ReviewResultSelector`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/ReviewResultSelector.java) 展示泛型方法和上界。
- [`LearningQueueTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/LearningQueueTest.java) 覆盖不同元素类型、只读返回和空队列访问。
- [`ReviewResultSelectorTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/ReviewResultSelectorTest.java) 覆盖具体类型保留、不同实现类和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 把泛型当成语法装饰：泛型的价值是类型安全和复用。
- 使用裸类型：编译器会少帮你发现类型错误。
- 为了“通用”到处使用 `Object`：这通常会带来强制转换和运行时风险。
- 忘记上界：泛型方法里无法调用目标接口的方法。
- 类型参数名过长或含义不清：常见单字母如 `T`、`E`、`K`、`V` 已经足够表达简单场景。
- 对空集合直接取第一个元素：泛型不替代业务边界校验。

调试泛型代码时，先看变量声明处的类型参数，再看方法签名中的类型参数和上界。多数泛型问题都能从“这里到底承诺保存什么类型”开始定位。

## 最佳实践及适用边界

- 集合和泛型类声明时写完整类型参数。
- 泛型类适合表达“同一套结构可以保存不同类型”。
- 泛型方法适合表达“同一段算法可以处理不同但相关的类型”。
- 需要调用某个接口能力时，为类型参数添加上界。
- 不要过早引入复杂泛型。初学阶段先掌握泛型类、泛型方法和上界即可。
- 通配符、协变逆变、类型擦除细节可以等后续工程实践再深入。

## 分级练习

- **基础**：实现 `LearningQueue<T>` 的 `add`、`peekNext`、`completeNext`、`size` 和 `isEmpty`。
- **进阶**：让 `remainingItems` 返回只读副本，避免外部修改队列内部状态。
- **挑战**：实现 `ReviewResultSelector.bestResult`，使用 `<T extends ReviewResult>` 保留具体返回类型。

## 面试与复习题

1. 泛型解决了什么问题？
2. `LearningQueue<T>` 中的 `T` 表示什么？
3. 为什么 `LearningQueue<CourseLesson>` 不能添加 `QuizResult`？
4. 菱形语法 `<>` 的作用是什么？
5. 泛型方法的类型参数写在哪里？
6. `<T extends ReviewResult>` 为什么能调用 `scoreRate`？
7. 裸类型有什么风险？
8. 泛型能替代空值和空集合校验吗？

## 本章总结

泛型让类型成为可复用代码的一部分。泛型类可以复用数据结构，泛型方法可以复用算法，上界可以约束类型必须具备某种能力。写清楚类型参数，编译器就能更早帮你发现类型错误。

## 下一步

上一章：[集合](./06-collections.md)

下一章：[文件 I/O](./08-file-io.md)

继续查看：[学习路线](../../roadmap/index.md)

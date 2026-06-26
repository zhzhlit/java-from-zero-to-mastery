---
title: 集合
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 集合

## 学习目标

- 解释数组和集合的核心差异。
- 使用 `List` 保存有顺序、可重复的元素。
- 使用 `Set` 保存不重复的元素。
- 使用 `Map` 建立键到值的查询关系。
- 返回只读集合，避免外部代码修改对象内部状态。
- 编写测试覆盖集合顺序、去重、查询和非法输入。

## 前置知识

已完成[异常处理](./05-exceptions.md)，能够阅读构造器校验、业务异常、失败后状态检查和 JUnit 断言。

## 为什么需要集合

数组长度固定，适合保存一组数量明确的数据。真实业务里，数据经常会增长、去重、按条件查询。例如课程目录需要：

- 按添加顺序保存课时。
- 给课时打多个标签。
- 标签不能重复。
- 根据标签快速找到对应课时。

这些需求都可以用 Java 集合表达。集合来自 `java.util` 包，最常用的三类是 `List`、`Set` 和 `Map`。

## List：保存有顺序的数据

`List` 表示一列元素，保留添加顺序，也允许重复。常见实现类是 `ArrayList`：

```java
private final List<CourseLesson> lessons = new ArrayList<>();
```

字段类型写 `List<CourseLesson>`，表示调用者只依赖列表能力；右边使用 `ArrayList`，表示内部选择一种适合按顺序追加和遍历的实现。

添加课时时，把对象放进列表：

```java
lessons.add(lesson);
```

统计总时长时，可以用增强 `for` 循环遍历：

```java
int total = 0;
for (CourseLesson lesson : lessons) {
    total += lesson.getDurationMinutes();
}
return total;
```

## Set：保存不重复的数据

`Set` 表示一组不重复元素。课程标签适合用 `Set`，因为同一个课时传入 `["core", "core"]` 时，目录里只应该保留一个 `core`。

```java
private final Set<String> tags = new LinkedHashSet<>();
```

这里使用 `LinkedHashSet`，它既能去重，也会尽量保留插入顺序，便于学习者阅读测试结果。

添加标签时，不需要自己判断是否已经存在：

```java
tags.add(tag);
```

如果标签已经存在，`Set` 不会再新增一份。

## Map：建立查询索引

`Map` 保存键和值的对应关系。课程目录可以用标签作为键，用这个标签下的课时列表作为值：

```java
private final Map<String, List<CourseLesson>> lessonsByTag = new LinkedHashMap<>();
```

添加课时时，先从 `Map` 中取出某个标签已有的课时列表。如果还没有，就创建一个新列表放回 `Map`：

```java
List<CourseLesson> taggedLessons = lessonsByTag.get(tag);
if (taggedLessons == null) {
    taggedLessons = new ArrayList<>();
    lessonsByTag.put(tag, taggedLessons);
}
taggedLessons.add(lesson);
```

这样查询某个标签时，就不必每次扫描全部课时：

```java
List<CourseLesson> taggedLessons = lessonsByTag.get(normalizedTag);
if (taggedLessons == null) {
    return List.of();
}
return Collections.unmodifiableList(new ArrayList<>(taggedLessons));
```

`List.of()` 会创建一个不可修改的空列表，适合表示没有查询结果。

## 集合中的类型

`List<CourseLesson>`、`Set<String>` 和 `Map<String, List<CourseLesson>>` 里的尖括号用于声明集合元素类型。这样编译器就知道：

- `lessons` 只能保存 `CourseLesson`。
- `tags` 只能保存 `String`。
- `lessonsByTag` 的键是 `String`，值是 `CourseLesson` 列表。

完整的泛型机制会在下一章展开。本章先掌握一个实用原则：集合字段和变量都应该写清楚元素类型，不要使用没有类型信息的裸集合。

## 校验后再修改集合

`CourseCatalog` 添加课时时先完成所有校验，再修改内部集合：

```java
public void addLesson(CourseLesson lesson, List<String> lessonTags) {
    if (lesson == null) {
        throw new IllegalArgumentException("lesson must not be null");
    }
    Set<String> normalizedTags = normalizeTags(lessonTags);

    lessons.add(lesson);
    for (String tag : normalizedTags) {
        tags.add(tag);
        // 更新标签索引
    }
}
```

`normalizeTags` 会拒绝 `null`、空列表和空白标签。如果某个标签非法，方法会在修改 `lessons` 之前抛出异常，目录不会留下半更新状态。

## 返回只读集合

对象内部的 `lessons`、`tags` 和 `lessonsByTag` 都是可变集合。如果直接返回字段，外部代码就能绕过校验修改目录。

更稳妥的做法是返回一份只读副本：

```java
public List<CourseLesson> lessons() {
    return Collections.unmodifiableList(new ArrayList<>(lessons));
}
```

这里先用 `new ArrayList<>(lessons)` 复制当前内容，再用 `Collections.unmodifiableList` 包装成不可修改列表。调用者可以读取结果，但不能 `add`、`clear` 或删除元素。

## 可运行代码

- [`CourseCatalog`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/CourseCatalog.java) 展示 `List`、`Set`、`Map`、遍历、去重和只读返回。
- [`CourseCatalogTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/CourseCatalogTest.java) 覆盖顺序保存、标签查询、重复标签、只读集合和非法输入。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 继续用数组处理数量会变化的数据：添加和删除都会变得笨重。
- 忘记集合元素类型：裸集合会让编译器少帮你发现错误。
- 把 `List` 当成去重工具：如果业务要求不重复，应考虑 `Set`。
- 依赖 `Set` 的固定顺序：顺序不是所有 `Set` 都保证，测试不要误把顺序当业务承诺。
- 直接返回内部可变集合：调用者可能绕过业务方法破坏对象状态。
- 修改集合前没有完成全部校验：异常发生后可能留下半更新数据。

调试集合代码时，重点观察集合大小、元素顺序、键是否归一化，以及异常抛出前后集合是否变化。

## 最佳实践及适用边界

- 需要顺序和重复元素时用 `List`。
- 需要去重或成员判断时用 `Set`。
- 需要按键查询时用 `Map`。
- 字段类型优先写接口，例如 `List`、`Set`、`Map`；创建对象时再选择具体实现类。
- 对外返回集合时，优先返回只读副本，保护对象内部状态。
- 在小数据量、简单一次性流程中，不必过早建立复杂索引；清晰循环往往更容易维护。

## 分级练习

- **基础**：实现 `CourseCatalog.addLesson`，把课时保存到 `List` 中，并统计总时长。
- **进阶**：用 `Set` 保存去重后的标签，支持 `containsTag`。
- **挑战**：用 `Map<String, List<Lesson>>` 建立标签索引，并让所有查询方法返回只读集合。

## 面试与复习题

1. 数组和集合各自适合什么场景？
2. `List`、`Set`、`Map` 的核心差异是什么？
3. 为什么字段类型常写成 `List`，而不是 `ArrayList`？
4. `Set` 如何帮助去重？
5. `Map` 的键和值分别表达什么？
6. 为什么添加数据前要先完成全部校验？
7. 直接返回内部集合有什么风险？
8. 什么时候值得为查询建立 `Map` 索引？

## 本章总结

集合让程序可以保存、去重、遍历和查询数量会变化的数据。`List` 关注顺序，`Set` 关注唯一性，`Map` 关注键值关系。结合输入校验和只读返回，集合代码既灵活，也能保持对象状态可靠。

## 下一步

上一章：[异常处理](./05-exceptions.md)

下一章：[泛型](./07-generics.md)

继续查看：[学习路线](../../roadmap/index.md)

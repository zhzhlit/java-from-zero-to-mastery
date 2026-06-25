---
title: 数组
status: verified
javaVersion: "21"
verifiedAt: "2026-06-25"
---

# 数组

## 学习目标

- 声明、创建和初始化一维数组。
- 使用下标、普通 `for` 和增强 `for` 遍历数据。
- 计算总和、平均值和最大值。
- 理解数组的引用语义，并在需要时进行防御性复制。
- 识别空数组、`null` 和数组越界的区别。

## 前置知识

已完成[方法](./05-methods.md)，能够声明参数、返回值并运行 JUnit 测试。

## 为什么需要数组

如果需要保存四个成绩，分别声明四个变量会让遍历和统计很麻烦：

```java
int[] scores = {88, 92, 76, 100};
```

数组把一组类型相同的数据放在固定长度的容器中。这里的 `scores` 长度为 4，可以统一传给统计方法。

## 声明、创建与初始化

```java
int[] scores = {88, 92, 76, 100};
int[] empty = new int[4];
```

`new int[4]` 创建四个元素，`int` 元素的默认值都是 `0`。数组创建后长度不可改变；需要动态增减元素时，后续课程会学习集合。

## 下标、长度与遍历

数组下标从 `0` 开始。合法下标范围是 `0` 到 `length - 1`：

```java
for (int index = 0; index < scores.length; index++) {
    System.out.println(scores[index]);
}
```

只需要读取元素、不需要下标时，可以使用增强 `for`：

```java
int total = 0;
for (int score : scores) {
    total += score;
}
```

循环条件必须是 `index < scores.length`。写成 `<=` 会在最后一次迭代访问不存在的元素。

## 数组作为方法参数和返回值

数组可以直接作为参数：

```java
public static int sum(int[] values) {
    int total = 0;
    for (int value : values) {
        total += value;
    }
    return total;
}
```

方法也可以返回数组。调用者拿到的是数组引用，因此需要明确返回原数组还是副本。

## 引用、别名与防御性复制

```java
int[] source = {4, 1, 3};
int[] alias = source;
alias[0] = 99;
```

`source` 与 `alias` 指向同一个数组，修改其中一个会从另一个引用看到。若方法不应改变调用者的数据，应先复制：

```java
int[] copy = Arrays.copyOf(source, source.length);
Arrays.sort(copy);
return copy;
```

这就是入门层面的防御性复制。

## Arrays 常用方法

`java.util.Arrays` 提供常见数组操作：

```java
Arrays.sort(scores);
int[] copy = Arrays.copyOf(scores, scores.length);
boolean same = Arrays.equals(scores, copy);
String text = Arrays.toString(scores);
```

`sort` 会原地修改数组；如果要保留原顺序，应先复制。

## 二维数组入门

二维数组适合表示简单表格：

```java
int[][] grades = {
        {88, 92},
        {76, 100}
};

for (int[] row : grades) {
    for (int grade : row) {
        System.out.println(grade);
    }
}
```

Java 的二维数组本质上是“数组的数组”，每一行长度可以不同。

## 可运行代码

- [`ArrayStatistics`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-basics/src/main/java/io/github/javamastery/basics/arrays/ArrayStatistics.java) 展示求和、平均值和最大值。
- [`ArrayCopying`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-basics/src/main/java/io/github/javamastery/basics/arrays/ArrayCopying.java) 展示排序副本。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-basics -am \
  -Dtest=ArrayStatisticsTest,ArrayCopyingTest test
```

## 常见错误与调试提示

- `ArrayIndexOutOfBoundsException`：查看异常中的下标，再检查循环条件。
- 把 `length` 当作最后一个下标：最后一个下标应为 `length - 1`。
- 空数组求平均值：没有元素时平均值没有定义，应明确拒绝。
- 数组为 `null`：这与长度为 0 的数组不同，调用方法前要确认契约。
- 排序后原数组变化：`Arrays.sort` 会修改传入数组。

调试循环时，可在条件行设置断点，观察 `index`、`values.length` 和 `values[index]`。

## 最佳实践及适用边界

- 循环边界优先写成 `index < values.length`。
- 只读遍历优先使用增强 `for`，需要下标时使用普通 `for`。
- 方法要明确 `null` 与空数组的处理规则。
- 不希望修改调用者数组时返回副本。
- 数组适合长度固定、元素类型相同的数据；频繁增删不是它的强项。

## 分级练习

- **基础**：计算 `{88, 92, 76, 100}` 的最低分和及格人数。
- **进阶**：返回一个倒序副本，并证明原数组没有变化。
- **挑战**：统计二维成绩表中每位学生的平均分，并为不等长行编写测试。

## 面试与复习题

1. 数组的合法下标范围是什么？
2. `length` 是方法还是字段？
3. 普通 `for` 与增强 `for` 分别适合什么场景？
4. 空数组和 `null` 有什么区别？
5. 为什么 `Arrays.sort` 可能产生意外副作用？
6. 什么是数组别名？
7. 什么情况下需要防御性复制？
8. Java 二维数组为什么可以每行长度不同？

## 本章总结

数组提供固定长度、按下标访问的同类型数据容器。掌握边界、遍历和引用语义，才能安全地完成统计与复制操作。

## 下一步

上一章：[方法](./05-methods.md)

下一章：[字符串与 StringBuilder](./07-strings-and-stringbuilder.md)

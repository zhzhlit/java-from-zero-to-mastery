---
title: 变量与数据类型
status: verified
javaVersion: "21"
verifiedAt: "2026-06-24"
---

# 变量与数据类型

## 学习目标

- 声明、初始化和使用变量。
- 区分八种基本类型与引用类型的入门语义。
- 正确处理整数除法、类型转换、溢出和浮点精度。
- 使用清晰命名和常量表达业务含义。

## 前置知识

已完成[开发环境与 IntelliJ IDEA](./02-development-environment-and-idea.md)，能够运行 Maven 测试。

## 真实问题场景

温度换算、金额计算和数据库 ID 看起来都是“数字”，但它们对范围、精度和转换的要求不同。选错类型可能不会立即报错，却会产生悄悄变化的错误结果。

## 核心概念

Java 是静态类型语言：变量在编译阶段有确定类型。

八种基本类型：

| 分类 | 类型 |
| --- | --- |
| 整数 | `byte`、`short`、`int`、`long` |
| 浮点 | `float`、`double` |
| 字符 | `char` |
| 布尔 | `boolean` |

引用类型变量保存的是一个引用值。引用值可以指向对象，也可以是 `null`；引用本身不是对象。

## 原理与设计思想

### 声明与初始化

```java
int age = 20;
final int monthsPerYear = 12;
var courseName = "Java 21";
```

`var` 只让编译器推断局部变量类型，不会让 Java 变成动态类型。变量含义不清楚时，不要为了少写几个字符使用 `var`。

### 数值转换

较小整数类型参与算术运算时通常提升为 `int`。窄化转换可能丢失信息：

```java
long value = Integer.MAX_VALUE + 1L;
int unsafe = (int) value;
int safe = Math.toIntExact(value); // 溢出时抛出异常
```

### 整数除法与浮点数

```java
double wrong = 9 / 5;       // 结果先按整数计算，为 1.0
double right = 9.0 / 5.0;   // 1.8
```

二进制浮点数不能精确表示所有十进制小数。金额计算通常使用最小货币单位整数或 `BigDecimal`，而不是 `double`。

## 可运行代码

`TemperatureConverter` 展示 `double` 运算：

```java
public static double toFahrenheit(double celsius) {
    return celsius * 9.0 / 5.0 + 32.0;
}
```

`TypeConversion` 使用标准库保护窄化转换：

```java
public static int toIntExact(long value) {
    return Math.toIntExact(value);
}
```

运行：

```bash
mvn -B -pl examples/java-basics -am \
  -Dtest='TemperatureConverterTest,TypeConversionTest' test
```

练习模块：

```bash
mvn -B -pl exercises/java-basics/starter -am compile
mvn -B -pl exercises/java-basics/solution -am test
```

## 常见错误与排查

- `9 / 5` 得到 `1`：两个操作数都是整数。
- `long` 强转 `int` 后变成负数：发生溢出，使用范围检查或 `Math.toIntExact`。
- 比较浮点数失败：不要直接依赖 `==`，测试时使用允许误差。
- 局部变量“可能未初始化”：Java 要求在读取前明确赋值。
- 空引用导致 `NullPointerException`：先确认引用是否允许为 `null`。

## 最佳实践及适用边界

- 默认整数用 `int`，超出范围或表达长期计数时用 `long`。
- 类型首先表达业务约束，其次才考虑内存大小。
- 常量名使用大写下划线，如 `MAX_RETRY_COUNT`。
- 避免魔法数字，为业务数值命名。
- 本章不深入对象内存布局和数值字节码。

## 分级练习

- **基础**：完成 starter 中的 `BmiCalculator`。
- **进阶**：为 BMI 增加非正数参数校验。
- **挑战**：设计一个不用 `double` 表示人民币金额的价格模型，并解释单位选择。

## 面试与复习题

1. Java 的八种基本类型是什么？
2. `var` 是否意味着动态类型？
3. 为什么 `9 / 5` 不是 `1.8`？
4. 强制类型转换可能带来什么风险？
5. 为什么金额通常不用 `double`？
6. 引用变量和对象是什么关系？
7. `final` 局部变量解决什么问题？

## 本章总结

类型不是语法装饰，而是对数据范围、精度和可用操作的约束。可靠代码会主动处理转换和边界，而不是依赖隐式结果。

## 下一步

上一章：[开发环境与 IntelliJ IDEA](./02-development-environment-and-idea.md)  
下一章：[流程控制](./04-control-flow.md)


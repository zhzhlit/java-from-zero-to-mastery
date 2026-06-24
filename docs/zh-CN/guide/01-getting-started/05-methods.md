---
title: 方法
status: verified
javaVersion: "21"
verifiedAt: "2026-06-24"
---

# 方法

## 学习目标

- 声明和调用有参数、有返回值或 `void` 方法。
- 理解重载、作用域和 Java 的值传递。
- 将长流程拆成职责清楚、可测试的方法。
- 识别常见的方法级代码坏味道。

## 前置知识

已完成[流程控制](./04-control-flow.md)。

## 真实问题场景

价格计算如果全部塞进 `main`，很快会混合输入、校验、计算和输出。方法让我们为一段行为命名，并建立可测试的输入输出边界。

## 核心概念

```java
public static double total(double price, int quantity) {
    return price * quantity;
}
```

- 方法名表达行为。
- 参数是方法接收的输入。
- 返回值是方法交给调用者的结果。
- `void` 表示没有返回值，不表示方法没有副作用。

## 原理与设计思想

### 方法重载

同一类中，方法名相同但参数列表不同：

```java
label("Java", "21");
label("Java", "21", " -> ");
```

返回值类型不同不能单独构成重载，因为调用处可能无法据此选择方法。

### Java 只有值传递

Java always passes arguments by value. For an object variable, the copied value is a reference; both copies may refer to the same object, but reassigning the parameter does not reassign the caller's variable.

中文表达：Java 始终按值传递参数。对象变量传入方法时，复制的是引用值；两个引用可以指向同一对象，但在方法内给参数重新赋值，不会改变调用者变量保存的引用值。

### 作用域

参数和局部变量只在声明它们的方法或代码块中可见。缩小作用域能减少误用。

## 可运行代码

`PriceCalculator` 将校验与计算封装为可测试方法；`TextFormatter` 展示重载和默认行为复用。

```bash
mvn -B -pl examples/java-basics -am test
```

练习 `TimeFormatter`：

```bash
mvn -B -pl exercises/java-basics/starter -am compile
mvn -B -pl exercises/java-basics/solution -am test
```

## 常见错误与排查

- 忘记 `return`：检查所有正常分支是否返回匹配类型。
- 方法重载调用模糊：避免参数类型过于相近或传入 `null`。
- 误以为交换两个参数能交换调用者变量：参数是值的副本。
- 方法参数过多：可能意味着缺少一个业务对象，或方法职责太多。
- 布尔参数不直观：`createUser(true)` 无法表达 `true` 的含义，优先使用清晰方法名或枚举。

## 最佳实践及适用边界

- 一个方法只做一件可清楚命名的事。
- 优先返回计算结果，把打印和输入留在边界层。
- 校验错误尽早抛出，错误信息说明约束。
- 重载用于同一概念的不同输入形式，不用于完全不同的行为。
- 小方法不是越多越好；无意义转发也会增加阅读跳转。

## 分级练习

- **基础**：完成秒数到 `HH:mm:ss` 的格式化方法。
- **进阶**：处理负数输入，并为 0、65、3661 秒编写测试。
- **挑战**：把一个包含输入、计算、打印的长方法拆成至少三个有名称的步骤，并说明每个方法的职责。

## 面试与复习题

1. 方法签名通常包含哪些部分？
2. `void` 方法能否修改对象状态？
3. 什么条件构成方法重载？
4. Java 为什么不是引用传递？
5. 参数重新赋值为什么不影响调用者变量？
6. 什么是局部变量作用域？
7. 哪些迹象说明方法可能太长？
8. 布尔参数为什么可能降低可读性？

## 本章总结

方法是最基本的行为边界。好的方法名称清楚、输入输出明确、边界经过验证，并能被独立测试。

## 下一步

上一章：[流程控制](./04-control-flow.md)  
本版本课程到此结束，请返回[学习路线](../../roadmap/index.md)查看后续阶段。

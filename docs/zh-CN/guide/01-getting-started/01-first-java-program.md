---
title: 第一个 Java 程序
status: verified
javaVersion: "21"
verifiedAt: "2026-06-23"
---

# 第一个 Java 程序

## 1. 学习目标

学完本章后，你应该能够：

- 说清楚一个最小 Java 程序由 `class`、`method` 和 `main` 入口组成。
- 使用 Maven 编译并测试示例模块。
- 使用 `java -cp` 从已编译的 classpath 中运行一个 Java 类。
- 读懂 `Hello, Java 21!` 这个输出从哪里来。
- 根据错误信息排查常见的 classpath、类名和包名问题。

## 2. 前置知识

开始前你只需要具备三点基础：

- 已安装 JDK 21，并能在终端执行 `java -version`。
- 已安装 Maven，并能在终端执行 `mvn -version`。
- 知道如何在仓库根目录打开终端并运行命令。

如果你还不熟悉 Maven，也没关系。本章只会用到一个命令：让 Maven 帮我们编译代码并运行测试。

## 3. 真实问题场景

你刚加入一个 Java 项目，第一件事通常不是立刻写复杂业务，而是确认本地环境能否跑通：JDK 版本是否正确、项目能否编译、测试能否通过、程序能否启动。

本章使用仓库中的第一个示例程序完成这条最小闭环。示例位于 `examples/first-java-program`。它只做一件事：在终端输出：

```text
Hello, Java 21!
```

这个小程序看起来简单，但它覆盖了 Java 入门最重要的三个问题：代码放在哪里、Java 从哪里开始执行、运行时如何找到类。

## 4. 核心概念

### class：把代码组织成一个“类”

Java 程序通常写在 `class` 中。你可以先把 `class` 理解成一个命名的代码容器：它把一组相关的数据和行为放在一起。

本章示例的类名是 `HelloJava`：

```java
public final class HelloJava {
}
```

这里的 `public` 表示这个类可以被外部访问；`final` 表示这个类在本示例中不打算被继承。对初学者来说，先记住：文件中的主要类通常要有一个清晰的名字，用来表达它负责什么。

### method：类中的一段可调用行为

`method`，也就是方法，是写在类里面的一段可调用代码。方法有名字、返回值类型和方法体。

示例中的 `greeting` 方法返回一段字符串：

```java
public static String greeting() {
    return "Hello, Java 21!";
}
```

你可以把它读成：“公开的、属于类本身的、返回 `String` 的方法，名字叫 `greeting`。”

### main：Java 应用的常见入口

当你用 `java` 命令运行一个普通 Java 应用时，Java 运行时会寻找这个固定形状的方法：

```java
public static void main(String[] args)
```

它就是程序入口。入口的意思是：程序从这里开始执行。

本章示例中的 `main` 方法会调用 `greeting()`，再用 `System.out.println` 打印结果：

```java
public static void main(String[] args) {
    System.out.println(greeting());
}
```

## 5. 原理与设计思想

Java 源代码文件通常以 `.java` 结尾。编译后，JDK 会生成 JVM 能执行的 `.class` 字节码文件。运行时执行的不是源文件本身，而是编译后的类。

这也是为什么本章分成两个动作：

1. 先用 Maven 编译并测试示例模块。
2. 再用 `java -cp` 指向编译产物目录，运行包含 `main` 方法的类。

`-cp` 是 classpath 的缩写。classpath 告诉 Java：“去这些位置找要运行的类以及它依赖的类。”如果 classpath 写错，或者类的完整名称写错，Java 就找不到入口类。

本项目把示例代码放在包 `io.github.javamastery.examples` 下，所以运行时必须使用完整类名：

```text
io.github.javamastery.examples.HelloJava
```

而不是只写：

```text
HelloJava
```

## 6. 可运行代码

示例源码在 `examples/first-java-program`。

核心代码如下：

```java
package io.github.javamastery.examples;

public final class HelloJava {

    private HelloJava() {
    }

    public static String greeting() {
        return "Hello, Java 21!";
    }

    public static void main(String[] args) {
        System.out.println(greeting());
    }
}
```

先在仓库根目录运行测试：

```bash
mvn -B -pl examples/first-java-program -am clean test
```

然后运行程序：

```bash
java -cp examples/first-java-program/target/classes io.github.javamastery.examples.HelloJava
```

预期输出：

```text
Hello, Java 21!
```

## 7. 常见错误与排查

### 错误一：找不到或无法加载主类

常见报错：

```text
Error: Could not find or load main class io.github.javamastery.examples.HelloJava
```

可能原因：

- 还没有运行 Maven 编译，`target/classes` 不存在。
- `-cp` 后面的目录写错了。
- 类的完整名称写错了。
- 当前终端不在仓库根目录，导致相对路径解析到了错误位置。

排查步骤：

1. 重新运行 `mvn -B -pl examples/first-java-program -am clean test`，确认编译成功。
2. 确认存在 `examples/first-java-program/target/classes`。
3. 确认运行命令使用完整类名 `io.github.javamastery.examples.HelloJava`。
4. 确认你在仓库根目录执行 `java -cp examples/first-java-program/target/classes io.github.javamastery.examples.HelloJava`。

### 错误二：只写了类名，没有写包名

错误命令示例：

```bash
java -cp examples/first-java-program/target/classes HelloJava
```

如果源文件顶部声明了：

```java
package io.github.javamastery.examples;
```

运行时就必须使用包名加类名：

```bash
java -cp examples/first-java-program/target/classes io.github.javamastery.examples.HelloJava
```

### 错误三：没有 main 方法

常见报错：

```text
Error: Main method not found in class ...
```

请检查入口方法是否严格写成：

```java
public static void main(String[] args)
```

大小写、返回值、`static`、参数类型都很重要。`Main`、`main()`、`main(String arg)` 都不是这个标准入口签名。

## 8. 最佳实践及边界

- 初学时建议先用 Maven 命令编译，再用 `java` 命令运行，这能帮助你分清“编译”和“运行”是两件事。
- 类名使用大驼峰命名，例如 `HelloJava`、`OrderService`。
- 方法名使用小驼峰命名，例如 `greeting`、`calculateTotal`。
- 对外可运行的 Java 应用通常需要一个清晰的 `main` 入口；但 Web 服务、测试、插件等程序可能由框架或工具负责启动，不一定需要你手写 `main`。
- `System.out.println` 适合入门示例和临时观察输出；真实工程中通常会使用日志框架记录运行信息。

本章示例故意保持很小：它不是为了展示复杂架构，而是为了验证 Java 21、Maven、编译产物和 classpath 这一条基础链路。

## 9. 分级练习

### 基础

修改 `greeting()` 的返回值，让程序输出你的名字，例如：

```text
Hello, Ada!
```

修改后重新运行测试和程序。思考：为什么测试可能会失败？你需要同步更新哪些期望值？

### 进阶

新增一个方法：

```java
public static String greeting(String name)
```

让它根据传入的名字返回问候语。然后保留原来的 `greeting()` 方法，让它继续返回 `Hello, Java 21!`。

目标是理解：一个类中可以有多个方法，方法可以通过参数接收外部输入。

### 挑战

让 `main` 方法读取命令行参数：

- 如果用户传入名字，输出 `Hello, <name>!`。
- 如果没有传入名字，输出 `Hello, Java 21!`。

提示：`main(String[] args)` 中的 `args` 就是命令行参数数组。完成后，尝试用不同参数运行程序，并补充对应测试。

## 10. 面试与复习题

1. Java 中 `class` 的作用是什么？为什么代码通常要放在类里？
2. 方法由哪些部分组成？`public static String greeting()` 分别表示什么？
3. `public static void main(String[] args)` 为什么被称为程序入口？
4. 源代码 `.java` 文件和编译后的 `.class` 文件有什么区别？
5. classpath 的作用是什么？为什么 classpath 错了会导致 Java 找不到类？
6. 如果类声明了 `package io.github.javamastery.examples;`，运行时为什么不能只写 `HelloJava`？
7. `System.out.println(greeting())` 这行代码先执行哪一部分？为什么？
8. 在真实项目中，什么时候适合使用 `main` 方法？什么时候可能由框架负责启动？

## 11. 本章总结

本章完成了第一个可验证的 Java 21 程序。你已经看到：

- `class` 用来组织 Java 代码。
- `method` 表示类中的一段可调用行为。
- `main` 是普通 Java 应用最常见的启动入口。
- Maven 可以帮助我们编译代码并运行测试。
- `java -cp` 运行程序时，classpath 和完整类名必须匹配编译后的包结构。

如果你能独立运行测试、启动程序，并解释 `Hello, Java 21!` 从哪里来，就已经掌握了继续学习 Java 的第一块地基。

## 12. 下一步

下一章建议学习 Java 的变量、基本类型和字符串拼接。它会回答一个更实际的问题：程序如何保存输入、处理数据，并把结果组织成可读的输出。

继续学习前，你也可以先完成本章三个练习，尤其是挑战题。它会让你第一次接触 `main(String[] args)` 中的 `args`，为后续理解输入、参数和程序状态打基础。

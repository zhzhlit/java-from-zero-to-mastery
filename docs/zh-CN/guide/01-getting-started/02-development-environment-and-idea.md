---
title: 开发环境与 IntelliJ IDEA
status: verified
javaVersion: "21"
verifiedAt: "2026-06-24"
---

# 开发环境与 IntelliJ IDEA

## 学习目标

- 确认终端和 Maven 实际使用 Java 21。
- 使用 IntelliJ IDEA 导入本仓库的 Maven 聚合项目。
- 在 IDEA 中运行程序、JUnit 测试并完成一次断点调试。
- 遇到环境问题时，能够用命令判断问题属于 JDK、Maven、IDEA 还是工作目录。

## 前置知识

完成[第一个 Java 程序](./01-first-java-program.md)，知道类、`main` 方法和 Maven 模块的基本含义。

## 真实问题场景

“代码完全一样，为什么同学能运行，我却报错？”多数入门环境问题并不是 Java 代码错误，而是实际使用了错误的 JDK、IDEA 没有同步 Maven，或命令运行目录不对。

## 核心概念

- **JVM**：执行 Java 字节码的虚拟机。
- **JRE**：运行 Java 程序需要的 JVM 和标准库；现代 JDK 发布方式不再强调单独安装 JRE。
- **JDK**：开发工具包，包含运行环境、`javac`、`java`、诊断工具等。
- **Maven**：读取 `pom.xml`，统一完成依赖解析、编译和测试。
- **Project SDK**：IDEA 为当前项目选择的 JDK。

## 原理与设计思想

IDEA 是开发界面，Maven 构建才是仓库的统一标准。CI 无法读取你的 IDEA 私有配置，所以关键结果必须能通过 Maven 命令复现。

先检查：

```bash
java -version
mvn -version
```

两条命令都应显示 Java 21。`java -version` 正确但 `mvn -version` 显示其他版本，通常说明 `JAVA_HOME` 或 IDEA 的 Maven Runner JDK 配置不同。

## 可运行操作

### 导入项目

1. IDEA 选择 **Open**，打开仓库根目录。
2. 等待 IDEA 识别根目录 `pom.xml`。
3. 打开 **Project Structure → Project**，将 Project SDK 设为 JDK 21。
4. 打开 Maven 工具窗口，点击重新加载。

不要提交 `.idea`、`*.iml` 或包含本机绝对路径的配置。

### 运行程序

打开 `HelloJava.java`，点击 `main` 方法旁的运行按钮。等价命令：

```bash
mvn -B -pl examples/first-java-program -am test
java -cp examples/first-java-program/target/classes io.github.javamastery.examples.HelloJava
```

预期输出：

```text
Hello, Java 21!
```

### 运行测试

打开 `HelloJavaTest.java`，点击测试类旁的运行按钮。仓库级验证：

```bash
mvn -B verify
```

### 断点调试

1. 在 `HelloJava.greeting()` 的返回语句左侧单击，设置断点。
2. 以 Debug 方式运行 `main`。
3. 使用 **Step Over** 执行当前语句。
4. 在 **Variables** 查看局部变量，在 **Frames** 查看调用栈。

## 常见错误与排查

### `release version 21 not supported`

编译器不是 JDK 21。先看 `mvn -version`，再修正 `JAVA_HOME` 或 IDEA Maven Runner 使用的 JDK。

### IDEA 中类全红，但 Maven 能通过

重新加载 Maven，确认 Project SDK 和 Module SDK 都是 21；必要时使用 **Invalidate Caches**，但不要把它当成第一步。

### `Could not find or load main class`

确认已经编译、类名包含完整包名，并从仓库根目录执行命令。

### Maven 找不到模块

`-pl` 的路径相对于 Maven reactor。命令应在包含根 `pom.xml` 的仓库根目录运行。

## 最佳实践及适用边界

- 项目统一声明 Java 版本，个人电脑不要靠“IDEA 能跑”判断兼容性。
- 先读完整错误信息，再搜索最后一行；根因经常出现在前面。
- IDEA 用于效率，Maven 用于可重复性。
- 本章只介绍基础调试；远程调试、性能分析和 JVM 诊断在高级阶段学习。

## 分级练习

- **基础**：在 IDEA 中运行 `HelloJava` 和 `HelloJavaTest`。
- **进阶**：故意将 Project SDK 切换为错误版本，观察错误后恢复 JDK 21。
- **挑战**：在终端分别记录 `java -version`、`mvn -version` 和 IDEA Project SDK，解释三者可能不一致的原因。

## 面试与复习题

1. JDK、JRE 和 JVM 的职责分别是什么？
2. 为什么项目在 IDEA 中能运行，不代表 CI 一定能通过？
3. `JAVA_HOME` 和 `PATH` 分别影响什么？
4. Maven reactor 是什么？
5. 断点、Step Over 和调用栈分别用于观察什么？
6. 为什么不提交 `.idea` 和 `*.iml`？

## 本章总结

稳定开发环境的判断标准不是“界面没有红线”，而是 Java 21、Maven、IDEA 和命令行对同一项目给出一致结果。

## 下一步

下一章学习[变量与数据类型](./03-variables-and-data-types.md)。


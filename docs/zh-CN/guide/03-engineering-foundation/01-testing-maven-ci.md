---
title: 测试、Maven 与工程化验证
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 测试、Maven 与工程化验证

## 学习目标

- 区分“程序能运行”和“工程可验证”。
- 使用 JUnit 5 固定业务规则、边界条件和失败路径。
- 理解 Maven reactor、模块坐标、`-pl`、`-am`、`test` 与 `verify` 的作用。
- 读懂 CI 失败信息，并把本地命令与 GitHub Actions 检查对应起来。
- 为后续数据结构、数据库、Web 和 Spring Boot 学习建立稳定验证习惯。

## 前置知识

已完成[Java 核心综合复盘](../02-java-core/12-java-core-review.md)，能够阅读类协作、文件 I/O、枚举、集合、`Optional` 和 JUnit 断言，并能在终端运行 Maven 命令。

## 真实问题场景

命令行课程管理已经能运行：

```bash
java -cp flagship-project/course-manager-cli/target/classes io.github.javamastery.flagship.cli.CourseManagerApp
```

但真实项目不能只靠“我运行了一下”。一次修改可能让课程查询、进度保存、文件读写或文档导航悄悄坏掉。工程化验证要回答的是：

- 哪些规则有自动测试保护？
- 修改一个模块时，应该运行哪些命令？
- 提交到 GitHub 后，CI 会不会复现同样的结果？
- 出错时，如何从失败日志定位到具体模块、测试和断言？

## 核心概念

### 单元测试

单元测试验证一个小边界的行为。它不应该依赖真实网络、数据库或随机时间。当前项目使用 JUnit 5，例如：

- `CourseCatalogTest` 验证课程编号、标签和阶段查询。
- `StudyProgressTest` 验证完成课时、重复记录和完成百分比。
- `ProgressFileStoreTest` 使用 `@TempDir` 验证文件保存和读取。

好的测试名称应该描述行为，而不是只写 `test1` 或 `works`。

### 成功路径、失败路径和边界值

只测成功路径是不够的。工程代码至少要覆盖三类情况：

- 成功路径：合法输入得到预期结果。
- 失败路径：非法输入抛出清晰异常，且状态不被破坏。
- 边界值：空集合、重复输入、最后一个课时、文件不存在等。

例如学习进度记录既要测试“完成一个课时”，也要测试“重复完成同一个课时不会重复计数”。

### Maven reactor

本仓库是 Maven 聚合工程。根 `pom.xml` 汇总多个模块：

```text
examples/
exercises/
flagship-project/
```

常用命令：

```bash
mvn -B verify
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl flagship-project/course-manager-cli -am test
```

其中：

- `-pl` 指定要构建的模块。
- `-am` 同时构建该模块依赖的上游模块。
- `test` 运行单元测试。
- `verify` 运行 Maven 生命周期中更完整的验证阶段。
- `-B` 使用 batch 模式，适合 CI 和复制日志。

### CI

CI 是远端的自动验证环境。它不读取你的 IDE 私有配置，所以本地结果必须能用命令复现。

本项目的基础检查包括：

```bash
mvn -B verify
npm run check:links --prefix website
npm run docs:build --prefix website
```

如果本地通过但 CI 失败，优先比较 JDK 版本、工作目录、环境变量和命令是否一致。

## 工程化验证流程

一次常规修改建议按这个顺序验证：

1. **改动前确认环境**

   ```bash
   java -version
   mvn -version
   ```

   Java 和 Maven 都应使用 Java 21。`mvn -version` 比 `java -version` 更重要，因为编译实际由 Maven 使用的 JDK 完成。

2. **运行最小相关模块**

   修改 Java 核心示例：

   ```bash
   mvn -B -pl examples/java-core -am test
   ```

   修改主项目命令行课程管理：

   ```bash
   mvn -B -pl flagship-project/course-manager-cli -am test
   ```

3. **运行全仓验证**

   ```bash
   mvn -B verify
   ```

4. **检查文档链接和构建**

   ```bash
   npm run check:links --prefix website
   npm run docs:build --prefix website
   ```

5. **提交前看 diff**

   ```bash
   git status --short
   git diff --check
   ```

   `git diff --check` 可以发现尾随空格等容易被忽略的问题。

## 可运行代码

本章复用主项目命令行课程管理作为验证对象：

- [`CourseCatalogTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/test/java/io/github/javamastery/flagship/cli/CourseCatalogTest.java)：课程查询、标签查询和阶段分组。
- [`StudyProgressTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/test/java/io/github/javamastery/flagship/cli/StudyProgressTest.java)：进度记录和完成百分比。
- [`ProgressFileStoreTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/test/java/io/github/javamastery/flagship/cli/ProgressFileStoreTest.java)：文件读写边界。
- [`CourseManagerAppTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/test/java/io/github/javamastery/flagship/cli/CourseManagerAppTest.java)：命令行入口输出。

运行：

```bash
mvn -B -pl flagship-project/course-manager-cli -am test
```

## 常见错误与排查

- 只运行 `main` 不运行测试：命令行输出正确，不代表边界规则正确。
- 只在 IDE 里点运行：CI 不能复现 IDE 私有配置。
- `java -version` 是 21，但 `mvn -version` 不是 21：修正 `JAVA_HOME` 或 Maven Runner JDK。
- 忽略失败日志最上面的模块名：先看哪个 Maven 模块失败。
- 看到一个测试失败就改生产代码：先确认测试描述的规则是否正确。
- 文档链接检查失败：链接目标必须存在，并注意 VitePress 的 `docs` 根目录。

## 最佳实践及边界

- 每次改动先运行最小相关命令，再运行全仓验证。
- 测试应覆盖规则，不追求把每一行实现都硬凑覆盖率。
- 文件、时间、随机数和外部服务都属于不稳定边界，应通过固定输入或临时资源控制。
- `verify` 适合提交前检查；日常开发可以先用模块级 `test` 加快反馈。
- CI 失败时先复现命令，再定位代码。

## 分级练习

- **基础**：选择 `CourseCatalogTest` 中一个测试，说明它验证了什么业务规则。
- **进阶**：为 `StudyProgress` 增加一个失败路径测试，例如未知课时不能被记录。
- **挑战**：为 `CourseManagerApp` 设计一个新命令的测试，再实现最小代码让测试通过。

## 面试与复习题

1. 单元测试和手工运行程序分别能证明什么？
2. 为什么失败路径测试和成功路径测试同样重要？
3. Maven reactor 的 `-pl` 和 `-am` 分别做什么？
4. `test` 和 `verify` 的适用场景有什么不同？
5. 为什么 CI 必须使用命令行可复现的验证方式？
6. `@TempDir` 适合解决什么测试问题？
7. 当 CI 失败但本地通过时，你会先检查哪些差异？
8. `git diff --check` 能发现哪类问题？

## 本章总结

工程化验证让学习项目从“代码能跑”进入“修改可控”。JUnit 固定业务规则，Maven 统一构建入口，CI 在远端复现验证结果。后续学习数据结构、数据库、Web 和 Spring Boot 时，这套验证习惯会比任何单个 API 更重要。

## 下一步

上一章：[Java 核心综合复盘](../02-java-core/12-java-core-review.md)

下一章：[JUnit 5 专项练习](./02-junit5-testing-basics.md)

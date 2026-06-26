---
title: Java 核心综合复盘
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# Java 核心综合复盘

## 学习目标

- 用一个小型业务项目串联 Java 核心阶段知识。
- 识别类、集合、异常、文件、时间、Stream、枚举和测试在真实代码中的职责边界。
- 使用 Maven 运行综合项目测试和命令行入口。
- 按验收清单复盘自己是否能独立完成 Java 核心阶段任务。
- 为后续工程基础、数据库、Web 和 Spring Boot 学习建立项目入口。

## 前置知识

已完成[枚举与常用标准库](./11-enums-standard-library.md)，能够阅读类协作、集合、异常、文件 I/O、日期时间、Stream、枚举、`Optional` 和 JUnit 断言。

## 为什么需要综合复盘

前面的章节按知识点拆开学习，每章都刻意保持小而清楚。真正写业务代码时，这些知识不会分开出现：一个课程管理程序可能同时需要课程对象、课时集合、学习进度、状态枚举、文件保存、时间记录和自动化测试。

本章不急着引入新框架，而是先做一个 Java 核心阶段的收束：确认你能把已经学过的 API 组合成一个边界清楚、可运行、可测试的小项目。

## Java 核心能力地图

可以用下面的问题检查自己是否真的掌握了 Java 核心阶段：

| 能力 | 复盘问题 | 项目中的体现 |
| --- | --- | --- |
| 类与对象 | 哪些数据和行为应该放在同一个类里？ | `Course` 保存课程信息并计算总时长 |
| 封装 | 外部代码能不能绕过业务方法改坏状态？ | 集合返回只读副本，进度只能通过方法记录 |
| 继承与接口 | 当前问题是否真的需要继承层次？ | 本项目暂不引入继承，保留后续扩展空间 |
| 异常 | 哪些输入应该立即拒绝？失败后状态是否安全？ | 课程编号、课时 ID、时间和重复课程都有校验 |
| 集合与泛型 | 列表、集合、映射分别适合保存什么？ | `List` 保存课时顺序，`Set` 保存去重标签，`Map` 保存课程索引 |
| 文件 I/O | 如何把内存状态保存到磁盘？ | `ProgressFileStore` 使用 `Properties` 保存学习进度 |
| 日期时间 | 业务时间如何避免用字符串硬凑？ | `LocalDateTime` 保存开始和最后学习时间 |
| Stream | 哪些查询适合声明式处理？ | 查找下一个未完成课时、按标签搜索课程 |
| 枚举 | 固定选项如何进入类型系统？ | `CourseLevel` 表达课程阶段 |
| JUnit | 哪些规则必须自动验证？ | 课程查询、进度记录、文件读写和命令行流程都有测试 |

## 综合项目：命令行课程管理

v0.3.11 新增主项目第一步：

```text
flagship-project/course-manager-cli
```

它不是完整社区系统，而是“在线学习与知识社区”主项目的最小业务闭环：

- 管理示例课程与课时。
- 支持按课程编号和标签查询。
- 记录某门课程已经完成的课时。
- 将学习进度保存到本地 `properties` 文件。
- 提供 JUnit 测试验证核心业务规则。
- 提供一个可运行的命令行入口。

本阶段刻意不引入 Web、数据库、登录、权限或前端页面。先把 Java 核心建模能力练扎实，后续再迁移到 Spring Boot 和数据库。

## 代码结构

- [`CourseLevel`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/CourseLevel.java)：用枚举表达课程阶段。
- [`Lesson`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/Lesson.java)：保存课时编号、标题和时长。
- [`Course`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/Course.java)：组合课时和标签，计算课程总时长。
- [`CourseCatalog`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/CourseCatalog.java)：管理课程索引、查询、排序和按阶段分组。
- [`StudyProgress`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/StudyProgress.java)：记录完成课时、完成百分比和摘要文本。
- [`ProgressFileStore`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/ProgressFileStore.java)：用标准库 `Properties` 保存和读取进度文件。
- [`CourseManagerApp`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/flagship-project/course-manager-cli/src/main/java/io/github/javamastery/flagship/cli/CourseManagerApp.java)：提供命令行运行入口。
- [`course-manager-cli` 测试](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/flagship-project/course-manager-cli/src/test/java/io/github/javamastery/flagship/cli)：覆盖查询、进度、文件存储和命令行流程。

## 运行与测试

从仓库根目录运行测试：

```bash
mvn -B -pl flagship-project/course-manager-cli -am test
```

运行命令行示例：

```bash
java -cp flagship-project/course-manager-cli/target/classes io.github.javamastery.flagship.cli.CourseManagerApp
```

也可以指定进度文件路径：

```bash
java -cp flagship-project/course-manager-cli/target/classes io.github.javamastery.flagship.cli.CourseManagerApp target/my-progress.properties
```

第一次运行会完成第一节课并保存进度，输出类似：

```text
课程数量: 2
总时长: 300 分钟
当前进度: Java 核心综合复盘 | 1/3 | 33% | last 2026-06-26 10:30
进度文件: target/course-manager-progress.properties
```

再次运行同一个进度文件，会继续完成下一节课。

## 复盘任务

按下面顺序阅读和修改代码：

1. 先读 `SampleCourses`，确认示例数据有哪些课程、标签和课时。
2. 阅读 `Lesson` 和 `Course`，找出所有输入校验。
3. 阅读 `CourseCatalog`，说明为什么课程编号用 `Map` 查询，标签用 `Set` 去重。
4. 阅读 `StudyProgress`，解释为什么完成课时不能直接暴露可变集合。
5. 阅读 `ProgressFileStore`，说明 `Properties` 文件里保存了哪些字段。
6. 运行测试，选择一个测试方法，故意改坏代码并观察失败信息，再恢复。
7. 运行命令行入口两次，查看进度文件变化。

## 验收清单

完成本章后，你应该能够做到：

- 解释每个类负责什么，不把文件读写、业务规则和命令行输出混在一个方法里。
- 为新增字段补齐构造器校验和对应测试。
- 判断何时用 `List`、`Set`、`Map`、`Optional` 和 `enum`。
- 使用 `@TempDir` 测试真实文件读写，且不污染项目目录。
- 使用 Maven 精确验证一个模块，而不是只依赖 IDE 的绿色标记。
- 说明为什么本阶段不引入数据库和 Web 框架。

## 常见错误与调试提示

- 直接返回内部可变集合：外部代码可能绕过业务方法修改状态。
- 把进度保存写进 `StudyProgress`：这会让业务对象依赖文件系统，不利于测试。
- 用字符串保存课程阶段：固定选项优先考虑枚举。
- 用 `null` 表示找不到课程：查询结果更适合返回 `Optional<Course>`。
- 文件测试直接写项目目录：优先使用 JUnit `@TempDir`。
- 跳过 Maven 测试只运行 `main`：命令行输出正确不代表业务边界都正确。

调试时先从失败测试入手：看输入、预期规则、实际状态，再决定是实现错了还是测试写错了。

## 分级练习

- **基础**：为 `CourseCatalog.coursesByLevel` 增加测试，确认每个阶段都存在分组。
- **进阶**：为 `StudyProgress` 增加 `nextLesson(Course course)`，返回下一节未完成课时。
- **挑战**：扩展命令行入口，支持 `list`、`progress`、`complete <lessonId>` 三个命令，并保持核心业务逻辑可测试。

## 面试与复习题

1. 为什么综合项目里不应该把所有代码都写进 `main` 方法？
2. `List`、`Set` 和 `Map` 在课程管理场景中分别解决什么问题？
3. 返回只读集合能防止哪类封装问题？
4. `Optional<Course>` 相比返回 `null` 有什么优势？
5. `Properties` 适合本阶段的原因是什么？它不适合解决什么问题？
6. 文件读写测试为什么应该使用 `@TempDir`？
7. 如果后续要接入数据库，哪些类最可能保持不变？
8. 这个命令行项目迁移到 Spring Boot 时，哪些对象会变成 Controller、Service 或 Repository 附近的角色？

## 本章总结

Java 核心阶段的关键不是背完 API 名称，而是能把类型、集合、异常、文件、时间、枚举和测试组合成边界清楚的小系统。命令行课程管理项目就是这个阶段的验收物：它足够小，可以逐行读懂；也足够完整，可以承接后续工程化和企业级开发。

## 下一步

上一章：[枚举与常用标准库](./11-enums-standard-library.md)

下一章：[测试、Maven 与工程化验证](../03-engineering-foundation/01-testing-maven-ci.md)

查看主项目后续演进：[在线学习与知识社区主项目路线](../../projects/flagship-roadmap.md)

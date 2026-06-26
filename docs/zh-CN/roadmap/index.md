---
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# Java 学习路线图

本路线采用十个阶段推进。每个主题必须标注内容状态，方便学习者判断是否可直接使用。

## 内容状态标签

- `planned`：已纳入计划，但未进入默认导航，也不承诺可学习。
- `draft`：已有初稿，结构和示例仍可能调整。
- `learning-ready`：目标、示例、练习和复习题齐备，可用于学习。
- `verified`：示例已在 Java 21 环境验证，表述经过复核。

## 十阶段路线

| 阶段 | 前置知识 | 阶段目标 | 交付物或验收结果 |
| --- | --- | --- | --- |
| 1. 开发准备 | 基本电脑操作、命令行概念 | 搭建 JDK 21、Maven、IDE、Git 与本地运行环境 | 能 clone 项目、运行测试、提交一次规范变更 |
| 2. Java 基础 | 开发环境可用 | 掌握语法、类型、控制流、方法、数组、字符串和简单调试 | 完成可运行的小程序与基础练习 |
| 3. Java 核心 | Java 基础 | 掌握面向对象、异常、集合、泛型、I/O、时间 API 和常用标准库 | 能设计小型模块并解释关键 API 选择 |
| 4. 计算机与工程基础 | Java 核心 | 建立数据结构、算法、网络、操作系统、测试和构建基础 | 能用 Java 实现常见结构，并为代码补充测试 |
| 5. 数据库与 Web | 工程基础、SQL 入门 | 掌握关系数据库、JDBC、HTTP、Servlet 与 Web 基础 | 能实现一个含持久化的简单 Web 功能 |
| 6. 企业级开发 | 数据库与 Web | 掌握 Spring Boot、配置、分层架构、事务、校验、日志和安全基础 | 能交付一个可测试、可配置的业务服务 |
| 7. 中间件与分布式 | 企业级开发 | 理解缓存、消息队列、搜索、RPC、一致性和分布式故障处理 | 能说明组件边界，并实现一个异步或缓存场景 |
| 8. Java 高级 | Java 核心、工程基础 | 深入并发、JVM、性能分析、内存模型、虚拟线程和模块化 | 能定位性能或并发问题，并给出可验证优化 |
| 9. 微服务与云原生 | 企业级开发、分布式基础 | 掌握服务治理、容器、配置、观测性、CI/CD 和弹性设计 | 能部署一个可观测的微服务示例 |
| 10. 扩展方向 | 前九阶段核心能力 | 面向架构、数据工程、Android、工具开发、开源协作等方向延伸 | 形成个人方向项目、技术文章或贡献记录 |

默认导航只收录 `learning-ready` 或 `verified` 内容。`planned` 和空主题只能出现在路线图或维护计划中。

## 当前可学习内容

v0.4.0 已发布以下连续学习内容：

1. [第一个 Java 程序](../guide/01-getting-started/01-first-java-program.md)
2. [开发环境与 IntelliJ IDEA](../guide/01-getting-started/02-development-environment-and-idea.md)
3. [变量与数据类型](../guide/01-getting-started/03-variables-and-data-types.md)
4. [流程控制](../guide/01-getting-started/04-control-flow.md)
5. [方法](../guide/01-getting-started/05-methods.md)
6. [数组](../guide/01-getting-started/06-arrays.md)
7. [字符串与 StringBuilder](../guide/01-getting-started/07-strings-and-stringbuilder.md)
8. [IntelliJ IDEA 调试](../guide/01-getting-started/08-debugging-with-intellij-idea.md)
9. [类与对象](../guide/02-java-core/01-classes-and-objects.md)
10. [封装](../guide/02-java-core/02-encapsulation.md)
11. [继承与多态](../guide/02-java-core/03-inheritance-and-polymorphism.md)
12. [接口](../guide/02-java-core/04-interfaces.md)
13. [异常处理](../guide/02-java-core/05-exceptions.md)
14. [集合](../guide/02-java-core/06-collections.md)
15. [泛型](../guide/02-java-core/07-generics.md)
16. [文件 I/O](../guide/02-java-core/08-file-io.md)
17. [日期与时间 API](../guide/02-java-core/09-date-time-api.md)
18. [Lambda 与 Stream API](../guide/02-java-core/10-lambda-streams.md)
19. [枚举与常用标准库](../guide/02-java-core/11-enums-standard-library.md)
20. [Java 核心综合复盘](../guide/02-java-core/12-java-core-review.md)
21. [测试、Maven 与工程化验证](../guide/03-engineering-foundation/01-testing-maven-ci.md)

Java 核心阶段已提供连续课程、分级练习和主项目命令行课程管理入口。计算机与工程基础已从测试、Maven 和 CI 验证开始；数据结构、算法、网络、操作系统、数据库与 Web 仍处于后续版本规划中。

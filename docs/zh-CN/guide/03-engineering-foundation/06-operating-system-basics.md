---
title: 操作系统基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-30"
---

# 操作系统基础

## 学习目标

- 理解程序、进程、进程状态和 CPU 时间片的基本关系。
- 能用简单模型解释轮转调度为什么能让多个任务交替运行。
- 掌握字节、KiB、MiB、GiB 等二进制内存单位。
- 能读懂常见类 Unix 文件权限字符串和八进制权限。
- 理解环境变量和 `PATH` 如何影响 Java 程序启动。
- 用 JUnit 5 测试验证 OS 相关规则，而不依赖当前机器状态。

## 前置知识

已完成[网络基础](./05-network-basics.md)，能把工程边界拆成稳定、可测试的小规则。

## 练习入口

本章对应仓库中的操作系统基础练习：

- [`exercises/operating-system-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/operating-system-basics/starter)：学习者编辑区。
- [`exercises/operating-system-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/operating-system-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/operating-system-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/operating-system-basics/solution -am test
```

## 为什么先做模型

真实操作系统状态会随机器、权限、负载和平台变化。学习第一步不需要读取真实进程表，也不需要修改真实文件权限。我们先用小模型练习：

1. 进程有哪些状态。
2. CPU 时间片如何轮流分配。
3. 内存单位如何换算。
4. 文件权限如何表达读、写、执行。
5. 环境变量如何影响程序配置。

这些规则能被单元测试稳定验证，之后再接触真实系统 API 会更顺。

## 程序、进程和状态

程序是磁盘上的代码和资源，进程是程序运行后的实例。一个进程通常会经历这些状态：

| 状态 | 含义 |
| --- | --- |
| `NEW` | 刚创建，尚未进入就绪队列 |
| `READY` | 准备好运行，等待 CPU |
| `RUNNING` | 正在占用 CPU 执行 |
| `WAITING` | 等待 I/O、锁或其他事件 |
| `TERMINATED` | 已结束 |

练习中的 `ProcessTable` 用进程快照统计运行中进程、状态数量和内存总量。

## CPU 时间片与轮转调度

单核 CPU 在某个瞬间只能运行一个任务。操作系统通过快速切换，让多个任务看起来“同时”推进。

轮转调度的核心规则很简单：

1. 每个进程最多运行一个固定时间片。
2. 如果进程还没完成，就排回队尾。
3. 如果进程已经完成，就不再进入队列。

练习中的 `RoundRobinScheduler` 不模拟真实线程，只根据剩余执行时间返回执行顺序。

## 内存单位

操作系统经常用二进制单位描述内存：

| 单位 | 字节数 |
| --- | --- |
| `B` | 1 |
| `KiB` | 1024 |
| `MiB` | 1024 * 1024 |
| `GiB` | 1024 * 1024 * 1024 |

练习中的 `MemorySize` 会解析 `512B`、`2 MiB`、`1 GiB` 这类文本，并判断某个内存占用是否不超过限制。

## 文件权限

类 Unix 权限常写成 9 个字符：

```text
rwxr-x---
```

前三位表示所有者，中间三位表示用户组，后三位表示其他人。每组三位分别是读、写、执行。

同一个权限也可以写成八进制：

| 符号 | 八进制 |
| --- | --- |
| `rwx------` | `700` |
| `rw-r--r--` | `644` |
| `rwxr-x---` | `750` |

练习中的 `FilePermission` 会解析符号权限，并转换为八进制字符串。

## 环境变量与 PATH

环境变量是操作系统传给进程的一组键值。Java 程序常通过它读取运行配置，例如：

- `JAVA_HOME`：JDK 安装位置。
- `PATH`：命令查找路径。
- `APP_ENV`：应用运行环境。

`PATH` 通常由多个目录组成。命令行输入 `java` 时，系统会按 `PATH` 中的目录顺序查找可执行文件。

练习中的 `EnvironmentVariables` 会读取必需变量、缺失时的默认值，并拆分 `PATH`。

## 分级练习

- **基础**：完成 `MemorySize`，掌握二进制内存单位。
- **进阶**：完成 `ProcessTable` 和 `RoundRobinScheduler`，理解进程状态和调度。
- **挑战**：完成 `FilePermission` 和 `EnvironmentVariables`，说明权限和启动环境如何影响程序。

## 常见错误与排查

- 把 `KiB` 当成 1000 字节：内存场景通常按 1024 进位。
- 以为进程一直处于 `RUNNING`：等待 I/O 时常会进入等待状态。
- 轮转调度忘记把未完成进程放回队尾：后续任务会饿死。
- 权限位顺序看反：每组三位都是读、写、执行。
- 忽略空环境变量：缺失和空白值通常都不能当作有效配置。

## 面试与复习题

1. 程序和进程有什么区别？
2. `READY`、`RUNNING`、`WAITING` 分别代表什么？
3. 时间片太大或太小分别可能带来什么问题？
4. `1 MiB` 等于多少字节？
5. `rwxr-x---` 对应的八进制权限是多少？
6. `PATH` 为什么会影响 `java` 命令能不能运行？

## 本章总结

操作系统基础帮助你理解 Java 程序运行在哪里、如何占用资源、如何被调度、如何读取环境。完成本章后，你已经能把常见 OS 规则写成小而稳定的 Java 测试。

## 下一步

上一章：[网络基础](./05-network-basics.md)

继续按照[学习路线](../../roadmap/index.md)进入数据库与 Web 基础。

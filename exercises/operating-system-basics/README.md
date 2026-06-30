# 操作系统基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的[操作系统基础](../../docs/zh-CN/guide/03-engineering-foundation/06-operating-system-basics.md)，再进入 `starter`。本练习不读取真实系统状态，重点是理解进程、内存、权限和环境变量背后的可测试规则。

验证 starter：

```bash
mvn -B -pl exercises/operating-system-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/operating-system-basics/solution -am test
```

## 练习

1. `MemorySize`：解析 `B`、`KiB`、`MiB`、`GiB`，并完成字节、KiB、MiB 换算。
2. `ProcessTable`：用进程快照统计运行中进程、状态数量、内存总量和 pid 查找。
3. `RoundRobinScheduler`：用固定时间片模拟轮转调度顺序。
4. `FilePermission`：解析 `rwxr-x---` 这类符号权限，并转换为八进制。
5. `EnvironmentVariables`：读取必需变量、默认值和 `PATH` 片段。

## 推荐顺序

1. 先完成 `MemorySize`，明确二进制内存单位。
2. 再完成 `ProcessTable`，理解进程状态和资源占用。
3. 完成 `RoundRobinScheduler`，观察时间片如何影响执行顺序。
4. 完成 `FilePermission` 和 `EnvironmentVariables`，把 OS 配置规则写成可测代码。

## 完成标准

完成本练习后，你应该能做到：

- 区分字节、KiB、MiB 和 GiB。
- 解释进程状态、CPU 时间片和内存占用的基本含义。
- 读懂常见文件权限字符串和八进制权限。
- 说明环境变量和 `PATH` 对 Java 程序启动的影响。
- 用单元测试验证 OS 相关规则，而不依赖当前机器状态。

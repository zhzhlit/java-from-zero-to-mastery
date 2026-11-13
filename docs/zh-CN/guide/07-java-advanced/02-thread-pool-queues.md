---
title: 线程池队列与拒绝策略
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-13"
---

# 线程池队列与拒绝策略

## 学习目标与入口

- 将工作线程数量与排队容量分开设计。
- 明确饱和、关闭与拒绝处理的区别，不静默丢弃需要结果的任务。
- 观察 CallerRuns 对提交线程、并发量与超时预算的影响。
- 正确处理排队取消、部分提交和关闭后的 Future。

先完成[并发基础与线程任务](./01-concurrency-basics.md)。本章扩展原模块，不新增第三方依赖。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics/starter)：补齐 BoundedTaskPools.newFixedPool，移除对应测试类的 `@Disabled`。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics/solution)：完整工厂、队列演示及启用测试。
- [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics#readme)：验证与运行命令。

```bash
mvn -B -pl exercises/concurrency-basics/starter -am test
mvn -B -pl exercises/concurrency-basics/solution -am verify
```

本章新增 10 项默认测试，加入锁练习后模块共 33 项；starter 的三个测试类分别禁用。

## 固定线程数不限制所有排队

上一章 newFixedThreadPool 的队列不是有界队列，任务持续提交时仍可能积压。本章显式创建 ThreadPoolExecutor，令 corePoolSize 等于 maximumPoolSize，配合 ArrayBlockingQueue，区分“正在工作”和“等待执行”。

本例工作线程为 1 到 32、队列容量为 1 到 1000，仅用于约束教学输入。线程数不能凭 CPU 核心数或某个固定倍数直接定为生产结论；任务占用内存也不只取决于队列元素数量。

普通 ThreadPoolExecutor 的一般路径是：先补核心线程，再尝试入队，队列满后才可能增加至最大线程数，最后进入拒绝处理。这里 core 等于 maximum，因此工人已满、队列也满时直接拒绝。可参阅 [JDK 配置说明](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)。

测试固定一个工作任务等待 gate，再排入一个任务，使“1 工人 + 1 队列”的饱和状态可重复观察，而不是靠大量循环碰运气。

## 两种显式策略

| 策略 | 饱和时 | 本例观察到关闭时 |
| --- | --- | --- |
| ABORT | 抛出 RejectedExecutionException | 同样抛出 |
| CALLER_RUNS | 在提交线程执行当前任务 | 抛出，不静默丢弃 |

原生 [CallerRunsPolicy](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.CallerRunsPolicy.html) 在关闭时会丢弃任务。若 submit 返回的 FutureTask 从未执行、也未取消，等待者可能得不到完成通知。因此本例提供带关闭检查的拒绝回调，而不是改变线程池调度引擎。

关闭检查与运行并非跨线程原子屏障。与 shutdown 竞争的提交仍需按执行器语义理解，不能据此保证“发起关闭之后任何任务都绝不会再执行”。

## CallerRuns 会改变调用位置

CallerRuns 是让提交方承担执行成本的一种反馈，但不是“没有成本的自动降速”。任务会运行在调用 submit/execute 的线程上，可能携带其线程上下文，甚至阻塞请求线程或事件循环。

本例测试确认溢出任务的线程就是提交线程，池的工作线程和队列并未增加。然而多个提交者可以同时执行溢出任务，因此工人数量不是整个系统的总并发上限。

通过 submit 运行的任务异常保存在 Future 中；直接 execute 的 CallerRuns 任务异常则可能直接抛给提交者。测试分别验证，不能只看 submit 没抛异常就认定任务成功。

## 等待预算不是提交的硬时限

BatchTaskRunner 用 invokeAll 共享一个预算，但 CallerRuns 可以让 execute 在提交过程中同步执行慢任务。这时还没进入通常理解的结果等待，调用方就已经被任务占住。

测试让提交者执行的任务停在 gate 上，传入 50ms 预算，等待超过预算后确认调用者仍未返回，再安全释放。此时晚返回的结果可能成功，也可能报告超时；被验证的是“不能保证按预算及时返回”，不是某个计时竞态的固定结果。

因此不能把 CallerRuns 放进延迟敏感链路后，仍宣称有一个通用硬截止时间。还需任务自身的 I/O 超时、协作取消与入口过载控制。

## 取消与关闭的三个区别

1. **取消 Future 不等于队列槽位立即释放**：被取消的 FutureTask 可能仍排队；可通过 purge 或后续取出回收空间。队列指标只是观察值，不能用先看 size 再提交消除竞争。
2. **shutdown 不等于立即终止**：它拒绝新工作，但让已接受任务继续排空；应等待终止，不只检查 isShutdown。
3. **shutdownNow 不等于取消全部 Future**：它尝试中断工人，并返回未启动任务。若返回项是需要结果的 Future，应显式取消，避免等待者一直等待；已运行任务的中断还可能表现为 ExecutionException 的 cause。

部分批次已排队后出现拒绝，invokeAll 会请求取消本批未完成工作；这不是业务事务回滚，也不会停止共享池中的无关任务。测试保留一个不属于本批的工作任务，验证拒绝不会误伤它。

## 验证与下一步

10 项新增测试覆盖参数、线程命名、饱和拒绝、CallerRuns 位置与错误、关闭拒绝、purge、两种关闭、部分提交和预算边界。所有 gate 在 finally 中释放，线程池由测试创建者关闭。

本章不实现自动扩容、在线调参、任务持久化或生产线程池监控。下一章：[锁与复合状态](./03-locks-composite-state.md)。

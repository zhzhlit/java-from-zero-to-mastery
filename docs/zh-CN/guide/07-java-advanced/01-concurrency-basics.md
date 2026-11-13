---
title: 并发基础与线程任务
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-13"
---

# 并发基础与线程任务

## 学习目标与入口

- 区分任务、执行任务的线程和等待结果的调用方。
- 使用 JDK 线程池提交 Callable，通过 Future 观察成功或失败。
- 解释批次超时、协作取消和线程池关闭之间的差别。
- 对共享计数使用原子操作，以可控顺序验证并发行为。

先掌握泛型、异常、Lambda 和[操作系统基础](../03-engineering-foundation/06-operating-system-basics.md)。[中间件综合实践](../06-middleware-distributed/11-middleware-basics-review.md)中的网络与 I/O 也会涉及并发，但本章先在独立 JDK 任务中建立边界，不同时引入新中间件。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics/starter)：完成 BatchTaskRunner.run 和 CompletionCounter.recordCompletion。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics/solution)：完整实现、演示与 13 项启用测试。
- [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics#readme)：命令、输入边界和异常约定。

```bash
mvn -B -pl exercises/concurrency-basics/starter -am test
mvn -B -pl exercises/concurrency-basics/solution -am verify
```

本章基础测试为 13 项，加入队列与锁练习后模块共 33 项。starter 的测试使用类级 `@Disabled`，完成后移除；不需要 Redis 或外部服务器。

## 任务不等于线程

Runnable 表达不返回值的工作，Callable 可以返回结果并抛出异常。ExecutorService 将任务交给工作线程，调用方得到 Future 等待结果；不必为每个任务手动创建线程。

```java
try (var executor = Executors.newFixedThreadPool(2)) {
    var runner = new BatchTaskRunner(executor);
    List<Callable<String>> tasks = List.of(() -> "Java", () -> "Redis");
    List<String> results = runner.run(tasks, Duration.ofSeconds(2));
}
```

本章只使用标准 JDK 执行器。BatchTaskRunner 借用执行器，创建者负责关闭，避免一个小工具意外关闭其他组件共享的线程池。固定线程池限制活跃工作线程数，但不是线程安全或吞吐提升的通用保证。

## 结果顺序与执行顺序

带时限的 invokeAll 返回与输入任务顺序对应的 Future；任务可以并发执行、以不同顺序完成。本例收集结果也保留输入顺序，不把打印先后当作业务顺序。

测试让第一个任务等待第二个任务发出信号。两个工作线程下第二个能完成，但结果仍为 first、second；若改为串行执行则无法满足等待条件。这样的验证比“看日志似乎交错了”更可靠。

只有在观察相应 Future 完成后，才能据此推断任务结果已经可见；Future.get 的内存一致性约定见 [JDK 文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html)。不要靠 sleep 猜测某个任务一定已完成。

## 一份预算，而不是 N 份

run 将 timeout 一次交给 invokeAll，不在每个 Future 上重新等待完整预算。预算到期时，未完成任务会被请求取消；返回结果仍可能是成功、失败或取消的组合。

本例若发现取消的 Future，就报告批次超时，即使另一任务更早失败。若没有取消，则按输入顺序 get，首个失败以 ExecutionException 及其 cause 传播。没有聚合所有异常，也不是一失败就停止所有任务的 fail-fast 实现。

调用方中断直接以 InterruptedException 传播，未完成任务被请求取消。任务内部主动抛 CancellationException 则仍是任务执行异常，不能误判为本次等待预算到期。

## 取消不是强制终止

```text
等待超时 -> 请求取消 Future
  -> 工作线程可能收到中断
    -> 任务主动响应并清理 -> 才真正结束
```

任务若忽略中断，仍可能继续运行。测试专门设置一个暂时不配合的任务：run 已报告超时，停止标记却仍未出现；测试随后释放 gate，让线程安全结束。这是受控反例，不是推荐写法。

因此超时不证明数据库写入已回滚，也不证明邮件未发出。外部副作用需要自己的幂等、超时与恢复策略。不能通过吞掉 InterruptedException、无条件重试或返回默认值掩盖故障。

Java 21 的执行器可在 try-with-resources 中关闭，但 close 要等待终止。对于不配合的任务，离开 try 块仍可能等待很久；不能把“批次预算”误当作整个资源生命周期的硬时限。关闭行为见 [ExecutorService 文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)。

## 共享计数为什么不能随便加一

普通 `count++` 包含读、计算、写，多线程可能覆盖彼此的结果。把字段改成 volatile 并不能让这个复合操作自动成为原子操作。

CompletionCounter 使用 AtomicInteger.incrementAndGet。测试让四个任务重叠启动，各增加 1000 次，等待所有结果后检查总数 4000；不写一个依赖偶发失败的竞争测试。

单次原子增加不意味着多个字段、多个步骤或业务事务整体原子。锁、复合状态和可见性将在后续展开；参阅 [AtomicInteger](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)。

## 验证与下一步

13 项默认测试覆盖输入顺序、并行重叠、工作线程数量、空批次、null 结果、列表不可修改、异常 cause、取消区别、超时、中断、拒绝提交和共享计数。CountDownLatch 控制协作关系，测试结束时释放信号并关闭执行器，不留下后台任务。

本章每批最多 1000 项，但多个调用仍可能累计排队；newFixedThreadPool 的队列并不是有界队列。线程数、队列、拒绝策略和负载治理不能只凭本例参数推断。

下一章：[线程池队列与拒绝策略](./02-thread-pool-queues.md)。其中会进一步验证 CallerRuns 可能阻塞提交，因此本章等待预算不能当作任意执行器上的硬截止时间。

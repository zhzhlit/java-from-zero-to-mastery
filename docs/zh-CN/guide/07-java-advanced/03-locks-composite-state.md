---
title: 锁与复合状态
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-13"
---

# 锁与复合状态

## 学习目标与入口

- 将业务不变量转成同一锁保护的检查、修改和读取。
- 理解单次原子操作不能替代多字段、多步骤的一致性。
- 使用可中断加锁和 finally 解锁，避免异常遗留锁。
- 返回不可变的一致快照，不把内部集合交给调用者。

先完成[并发基础](./01-concurrency-basics.md)与[线程池队列](./02-thread-pool-queues.md)。本章继续扩展同一模块，不新增依赖或手写锁实现。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics/starter)：完成 CourseSeatLedger 的五个业务方法，校验辅助方法和快照类型已提供。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics/solution)：完整账本、LockStateDemo 与启用测试。
- [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/concurrency-basics#readme)：验证命令和完整 API 约定。

```bash
mvn -B -pl exercises/concurrency-basics/starter -am test
mvn -B -pl exercises/concurrency-basics/solution -am verify
```

本章新增 10 项测试，模块共 33 项。starter 对应三个测试类各自使用 `@Disabled`，完成后移除相应标记。

## 先写出不变量

课程名额账本同时保存容量、余量和已预订成员：

```text
capacity = available + learners.size()
0 <= available <= capacity
每个学员至多持有一个名额
```

若先检查余量，再在另一个临界区修改，两名学员可能同时看到最后一个名额。若成员集合和计数分别使用独立原子操作，仍不自动保证“检查重复、加入成员、扣减余量”整体一致。

本例让所有这些状态都归同一个私有 ReentrantLock 管理，成员集合用普通 HashSet 即可，但不能绕过锁读取它。锁住错误对象，或只锁写入不锁读取，都不满足这个设计。

## 业务方法的原子边界

| 方法 | 同一锁内完成的动作 |
| --- | --- |
| reserve | 先检查重复，再检查余量，添加成员并扣减 |
| cancel | 移除存在的成员，只释放一次名额 |
| transfer | 检查来源/目标，再替换成员，名额数不变 |
| resize | 检查已预订人数，再调整容量与余量 |
| snapshot | 同时读取字段并复制成员集合 |

满额时已有学员仍返回 ALREADY_RESERVED，而不是再占名额或变成新的 FULL。转让仅限一个账本内的成员替换，不是两个账本之间的事务，也没有跨锁转账。

校验失败发生在修改之前，所以已知业务拒绝不留下部分修改；但这不意味着锁会自动回滚任意异常。临界区不包含外部 I/O、网络调用或用户回调，避免把长耗时和未知异常带入锁保护范围。

## 先获得锁，再进入 try/finally

```java
lock.lockInterruptibly();
try {
    // 检查并修改由同一把锁保护的状态
} finally {
    lock.unlock();
}
```

获得锁后立即使用 finally 保证释放；如果加锁被中断而未成功，不能再执行 unlock。这里的修改方法将 InterruptedException 向上传播，不把取消信号变成“预订失败但继续运行”。

预先已中断的调用也会在加锁处失败，不修改业务状态。测试验证四种修改方法都遵守这一点；不能据此声称已经验证所有争用时机下的中断延迟。加锁和中断规则见 [ReentrantLock 文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)。

snapshot 选择普通 lock，因此可能等待锁，且不是可中断读取。本例不承诺公平顺序、无等待、固定等待上限或任何线程一定先成功。

## 为什么要一次返回快照

分开调用 getAvailable 和 getLearners，即使每个方法分别加锁，两次读取之间也可能发生修改。不能拿两个不同时间的值拼成同一业务状态。

Snapshot 在一次临界区内读取容量、余量与集合，并用 Set.copyOf 复制集合。调用者不能修改返回集合，旧快照也不会随以后取消、预订或扩容变化。公开快照类型还检查数值不变量，但快照本身不是实时订阅。

## 验证观察点

10 项测试包括：100 个任务争用 10 个名额；32 个任务争用同一学员标识；取消只释放一次；转让与调整容量的失败不改变状态；旧快照不变；预先中断不修改；异常之后另一线程仍能操作；反复修改时读取一致快照；独立账本互不影响。

异常释放锁测试特意让另一个工作线程继续操作，避免同一线程因锁可重入而掩盖未释放问题。所有异步等待有界，并在 finally 中取消测试任务，不留下等待锁的线程。

这些测试使用启动协调制造争用，但不是所有线程调度的形式化证明，也不是性能基准。演示只输出成功/满额数量，不依赖哪几个学员抢到名额。

## 边界与下一步

多个 Java 实例即使操作同一数据库，各自的 ReentrantLock 也互不认识。真实共享名额仍需要数据库约束、事务和跨进程恢复；不能用本例内存锁取代前面的可靠业务链路。

接下来按[学习路线](../../roadmap/index.md)学习 Java 内存模型与安全发布，再继续 JVM 和虚拟线程。

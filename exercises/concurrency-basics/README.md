# 并发基础与线程任务练习

使用 Java 21 的 ExecutorService、Callable、Future 和 AtomicInteger，学习任务提交、结果等待、失败传播、协作取消与共享计数。不新增第三方依赖，不手写线程池或调度引擎。

## 任务与验证

- BatchTaskRunner.run：校验并复制任务列表，使用一个批次预算执行，按输入顺序收集结果。
- CompletionCounter.recordCompletion：用原子操作增加完成计数，避免普通 `count++` 的竞争。
- BoundedTaskPools.newFixedPool：配置有界队列，选择显式拒绝或带关闭检查的 CallerRuns。
- CourseSeatLedger：用同一把锁保护名额、成员集合、转让与容量调整，返回一致快照。

starter 提供可编译骨架，33 项测试在三个测试类上使用 `@Disabled`；完成后移除对应标记。solution 完整实现并启用全部测试，其中基础任务 13 项、有界队列 10 项、锁与复合状态 10 项。

```bash
mvn -B -pl exercises/concurrency-basics/starter -am test
mvn -B -pl exercises/concurrency-basics/solution -am verify
java -cp exercises/concurrency-basics/solution/target/classes io.github.javamastery.exercises.concurrency.ConcurrencyBasicsDemo
```

演示输出：

```text
Results in input order: [Java, Redis, RPC]
Completed tasks: 3
Executor terminated: true
```

测试使用真实 JDK 线程池和 CountDownLatch 控制先后关系，不靠反复随机运行等待竞争出现。覆盖重叠执行与有序结果、空批次、null 返回值、列表不可修改、工作线程数量、异常 cause、任务主动抛取消异常、整体超时、调用方中断、不配合取消的任务、参数校验、拒绝执行和 4000 次共享计数。

## API 约定

run 借用调用方的 ExecutorService，不自行关闭。任务最多 1000 项，timeout 必须大于零且不超过 1 分钟；所有参数先校验，任务列表做快照，不接受 null 任务。Callable 结果可以为 null，返回列表不可修改，但不会深复制任务返回的对象。

使用带时限的 invokeAll，预算属于整个批次，不是逐个 Future.get 重新分配。列表仍按输入顺序返回，完成顺序可以不同。此实现不是 fail-fast：某个任务失败后仍可能等其他任务完成或预算到期。

| 情况 | 对调用方的行为 |
| --- | --- |
| 全部成功 | 返回输入顺序的结果 |
| 全部结束，其中任务失败 | ExecutionException 保留原 cause；只暴露首个输入顺序的失败 |
| 批次到期有任务被取消 | TimeoutException；优先于同批其他任务失败 |
| 调用方被中断 | InterruptedException 直接传播，未完成任务被请求取消 |
| 执行器拒绝提交 | RejectedExecutionException 不伪装为空结果 |

任务自己抛 CancellationException 是任务执行失败，会包装为 ExecutionException；不等于 invokeAll 取消其 Future。

## 取消与关闭边界

取消是请求，不是强制停止，也不会回滚已发生的外部副作用。生产任务应响应中断、设置 I/O 超时并在 finally 释放资源。测试中的反例故意暂时忽略中断，但最终总会释放 gate，不把这种写法作为生产示例。

Java 21 的 ExecutorService 可用于 try-with-resources；close 会等待任务终止，因此不能将它当成任意任务的有界终止器。BatchTaskRunner 报超时，不代表所有线程已停止，更不代表方法外层的 close 必然立即返回。

固定线程数不等于有界队列。基础示例使用 newFixedThreadPool，批次上限也不限制多个并发调用累计的排队量。BatchTaskRunner 本身不配置执行器队列或拒绝策略，也没有结果聚合、自动重试、并发事务、虚拟线程或业务副作用去重。

## 有界队列与拒绝策略

BoundedTaskPools 创建 core/maximum 相同的 ThreadPoolExecutor，使用 ArrayBlockingQueue、具名非 daemon 工作线程。工作线程为 1 到 32，队列为 1 到 1000；这些是教学输入边界，不是生产调优推荐。

- ABORT：饱和或关闭时抛出 RejectedExecutionException，已经接受的任务不会被本策略丢弃。
- CALLER_RUNS：饱和时由提交线程执行；拒绝处理器观察到线程池已关闭时抛出异常，避免原生 CallerRunsPolicy 在关闭时静默丢弃 FutureTask。本例不提供与关闭并发提交的全局停止屏障。

```bash
java -cp exercises/concurrency-basics/solution/target/classes io.github.javamastery.exercises.concurrency.ThreadPoolQueueDemo
```

```text
ABORT queued: 1
ABORT overflow rejected: true
CALLER_RUNS queued: 1
CALLER_RUNS executes on caller: true
```

CallerRuns 不是总并发上限：工作线程之外，多个提交者也能执行任务。它会阻塞 submit/execute 本身，因此 BatchTaskRunner 的等待预算不能保证整个调用在预算内返回；测试固定持有提交者执行的任务，观察 50ms 预算后提交线程仍阻塞，再显式释放。返回可能成功或报告超时，但都不能当作及时返回。

取消排队 Future 不一定立即腾出队列空间；purge 可移除已取消的任务。shutdown 排空已接受队列并拒绝新任务；shutdownNow 尝试中断工作线程并返回尚未启动的 Runnable，并不自动将所有关联 Future 标为取消。需要等待这些 Future 的调用方应显式取消返回的 Future 任务，不能无限 get。

有界批次部分提交后遇到拒绝时，invokeAll 会请求取消本批已提交的任务，不会替你停止池里无关的业务。取消不能撤销已经完成的副作用。队列长度和活跃线程数是观察值，不应拿检查队列再提交替代真实拒绝处理。

- [ThreadPoolExecutor](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)
- [原生 CallerRunsPolicy](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.CallerRunsPolicy.html)

## 锁与复合状态

CourseSeatLedger 维护 `capacity = available + learners.size()`。容量为 1 到 10000，学员标识为 1 到 64 位 ASCII 字母、数字、下划线或连字符。所有共享读写使用同一个私有 ReentrantLock，不依靠多个独立原子字段拼出业务一致性。

reserve 区分预订成功、已预订、满额；重复学员不再占名额。cancel 不存在时返回 false。transfer 在同一账本中替换成员，双方必须不同，来源不存在或目标已有预订时不修改状态。resize 不能小于已预订人数，校验成功后一起调整容量和余量。

四个修改方法使用 lockInterruptibly，向调用方传播 InterruptedException；获得锁后始终 finally 解锁。snapshot 使用普通 lock，可能等待，不承诺可中断读取；其集合是复制后的不可变集合，不暴露内部 HashSet，也不会随以后修改改变。

```bash
java -cp exercises/concurrency-basics/solution/target/classes io.github.javamastery.exercises.concurrency.LockStateDemo
```

```text
Reserved: 5
Full: 15
After transfer reserved: 5
After cancellation and resize available: 3
```

新增 10 项测试覆盖并发不超卖、重复预订、幂等取消、转让、调整容量、一致快照、预先中断、异常后其他线程继续操作和独立账本状态。协调开始不等于穷尽所有线程调度；测试不承诺谁先预订成功，也不验证锁公平性或性能。

这是同一 JVM 中、同一账本实例的保护，不是跨实例或跨进程不超卖方案。锁不自动回滚异常前的修改，不提供外部 I/O、数据库事务或分布式幂等；不要在锁内调用外部服务或不受控回调。

- [ReentrantLock](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReentrantLock.html)

- [ExecutorService](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
- [Future](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html)
- [AtomicInteger](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicInteger.html)

# 缓存基础练习

使用 Caffeine 3.1.8（版本由现有 Boot BOM 管理）实现进程内课程目录缓存，不手写淘汰算法。

## 任务

- 基础：完成 `findById`，使用 `Cache.get` 原子加载，不缓存缺失值或异常。
- 进阶：先更新源数据，成功后失效对应缓存；提供显式失效入口。
- 挑战：解释容量限制、写入后过期、统计和同键并发加载的作用及边界。

starter 的三个缓存操作是可编译骨架，11 项测试在类级 `@Disabled` 下禁用。容量、TTL、时钟注入、不可变 Course 与计数数据源已提供。solution 完整实现同样的 11 项测试。

## 验证

```bash
mvn -B -pl exercises/cache-basics/starter -am test
mvn -B -pl exercises/cache-basics/solution -am test
```

预期 starter 跳过 11 项，solution 通过 11 项。过期测试使用可控 Ticker，不调用 sleep；容量测试先执行维护，不假定具体被淘汰的键；并发测试验证同键成功加载只读取数据源一次。

## 运行示例

在仓库根目录运行，macOS/Linux 的 classpath 分隔符为冒号，Windows 请改为分号：

```bash
mvn -B -pl exercises/cache-basics/solution -am compile
mvn -B -f exercises/cache-basics/solution/pom.xml dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
java -cp 'exercises/cache-basics/solution/target/classes:exercises/cache-basics/solution/target/dependency/*' io.github.javamastery.exercises.cache.CacheBasicsDemo
```

关键输出：

```text
Source queries after two reads: 1
Source queries after rename: 2
Cache hits: 1
```

数据源是内存模拟，不是数据库。本章只保证演示中的顺序更新行为，不承诺并发读写强一致性、跨实例失效或事务提交后失效。缓存不作为报名名额、授权或用户数据的事实来源。

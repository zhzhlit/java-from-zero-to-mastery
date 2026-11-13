---
title: 搜索基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-11-12"
---

# 搜索基础

## 学习目标

- 区分数据库查询与全文检索，理解索引是可重建的派生数据。
- 使用 Lucene 完成课程文档的新增、同 ID 更新、删除与搜索。
- 保持索引和查询分析器一致，组合全文匹配与精确条件。
- 用真实索引验证 refresh、commit 与关闭重开的差别。

## 前置知识与入口

已掌握集合、I/O、异常及数据库，建议先完成[可靠发布与 Outbox 衔接](./08-outbox-stream-publishing.md)，理解派生状态更新为什么需要恢复依据。本章不直接连接消息模块，先将搜索本身做成独立、可验证的小练习。

- [starter](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/search-basics/starter)：资源初始化、关闭、数据类型已提供，补齐五个核心方法。
- [solution](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/search-basics/solution)：完整实现、SearchBasicsDemo 和 14 项启用测试。
- [模块 README](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/search-basics#readme)：验证、演示和 API 约定。

依赖只在新模块管理，固定为 Lucene 10.3.1 的 core 与 analysis-common。其[系统要求](https://lucene.apache.org/core/10_3_1/SYSTEM_REQUIREMENTS.html)为 Java 21 或更高，与本项目基线一致；不需要额外搜索服务器。

```bash
mvn -B -pl exercises/search-basics/starter -am test
mvn -B -pl exercises/search-basics/solution -am verify
```

starter 的 14 项测试使用类级 `@Disabled`；完成后移除。solution 使用 Lucene ByteBuffersDirectory 与 FSDirectory 的真实行为，不用 Map 模拟搜索引擎。

## 搜索不是换一种写法的 LIKE

数据库适合保存权威业务数据和执行事务。搜索引擎将文本分析为词项，建立词项到文档的倒排关系，再根据查询计算相关性。它通常不是任意子串匹配，也不天然与数据库同步。

本章只建立课程搜索投影。生产中的数据库仍需保留权威来源；索引丢失时应能重建，不能把“搜索不到”直接解释成业务记录不存在。

## 字段设计

| 字段 | Lucene 类型 | 用途 |
| --- | --- | --- |
| id | StringField，存储 | 精确更新/删除和返回业务 ID，不分词 |
| level | StringField，存储 | 精确级别过滤，不参与相关性评分 |
| title、description | StoredField | 取回显示原文，不能单靠 StoredField 搜索 |
| content | TextField，不存储 | 合并标题与描述，提供分析后的全文搜索 |

`updateDocument(new Term("id", id), document)` 替换同 ID 文档，不是不断追加同一课程。delete 按精确 ID 删除，删除不存在的 ID 不报错。输入边界由 CourseDocument 和索引方法校验。

## 索引和查询使用同一分析器

本例使用 [CJKAnalyzer](https://lucene.apache.org/core/10_3_1/analysis/common/org/apache/lucene/analysis/cjk/CJKAnalyzer.html)：支持英文大小写归一和 CJK 双字组合，并过滤默认停用词。它不是中文词典分词，更不是语义理解；单字、同义词、拼音和任意子串不在本章保证范围内。

[QueryBuilder](https://lucene.apache.org/core/10_3_1/core/org/apache/lucene/util/QueryBuilder.html)直接从分析器构建查询，不解释用户输入中的字段或通配符语法。`JAVA REDIS` 要求分析后的两个词项都出现，可分布在标题和描述中，但不要求连续或顺序。

输入 `*:*` 不会变成“匹配所有文档”；停用词过滤后没有词项也返回空结果。空白输入、过长输入和非法 limit 则报错。这些区别需要明确约定，不能无意把无效搜索变成全量数据查询。

级别通过 BooleanQuery 的 FILTER 条件精确限制。它不是授权控制，不能用 BEGINNER/ADVANCED 过滤替代租户或用户权限。

## 可见性与持久化是两件事

| 操作 | 当前搜索快照 | 文件提交点 |
| --- | --- | --- |
| upsert/delete | 不自动变化 | 不自动提交 |
| refresh | 看到当前写入及删除 | 不提交 |
| commit | 不自动刷新旧快照 | 保存当前提交点 |
| close | 关闭资源 | 本封装不自动提交，丢弃提交点后的变更 |

本封装使用 SearcherManager 管理近实时快照，通过 acquire/release 成对借还查询器；refresh 调用 maybeRefreshBlocking。本练习显式配置 `setCommitOnClose(false)`，避免把关闭资源误当成业务提交。与直接使用 Lucene 默认关闭行为区分开来。

```java
try (Directory directory = FSDirectory.open(path);
     CourseSearchIndex index = new CourseSearchIndex(directory)) {
    index.upsert(new CourseDocument(1, "Java 缓存", "Redis basics", CourseLevel.BEGINNER));
    index.refresh();
    var result = index.search("缓存", CourseLevel.BEGINNER, 10);
    index.commit();
}
```

Directory 归调用方所有，必须晚于索引对象关闭。第二个写入器尝试打开同一目录会失败，不能通过忽略锁错误来“解决”。文件测试仅证明有序关闭重开，不证明操作系统崩溃、磁盘故障或掉电恢复。

## Top-N 与总数

结果包含按相关性排列的有界命中列表，以及当前快照精确总数。Lucene 的 TopDocs.totalHits 在超过阈值时可能只是下界；本例另在同一查询快照上调用 count，避免将下界误报为精确数。

测试放入 1100 个课程、只取 3 个结果，仍要求总数为 1100。这种精确计数增加查询开销，生产需按需求权衡；同分结果没有稳定业务排序，不支持深分页。本封装同步所有公开操作，以清晰展示生命周期，不用于性能基准。

## 练习与验证

1. 完成文档字段映射和按 ID 更新，验证旧词项消失。
2. 完成中文与英文 AND 查询、级别过滤和输入边界。
3. 在删除后分别于 refresh 前后查询，观察旧快照。
4. 对比 commit 后未 refresh、refresh 后未 commit 两种状态。
5. 关闭重开文件索引，验证只恢复最近提交的内容。

14 项默认测试还检查空索引、原文字段返回、普通文本输入、不变结果列表、目录所有权、关闭后拒绝操作以及第二个写入器冲突。运行示例的预期输出见模块 README。

## 边界与下一步

本章没有数据库到索引的自动同步、索引版本迁移、权限过滤、后台刷新、分布式分片、副本或在线重建。I/O 与格式错误直接抛出，不能返回“空结果”掩盖索引故障。索引同步、去重和重建应复用此前学到的可靠事件与幂等思想，但不是本章暗中实现的能力。

继续按照[学习路线](../../roadmap/index.md)进入 RPC 基础，然后进行中间件综合实践。

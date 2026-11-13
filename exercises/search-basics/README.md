# 搜索基础练习

使用 Apache Lucene 10.3.1 的 `lucene-core` 与 `lucene-analysis-common`，在 Java 21 上实现课程搜索。不手写倒排索引、分词器或评分引擎，也不需要 Elasticsearch 服务。

## 任务

- 基础：区分 StringField、TextField 与 StoredField，按课程 ID 更新和删除文档。
- 进阶：使用同一 CJKAnalyzer 构建索引和查询，支持中英文关键词及精确级别过滤。
- 挑战：区分 refresh、commit 和 close，验证文件索引重开及有界结果与精确总数。

starter 提供数据类型、资源初始化和关闭逻辑；补齐 CourseSearchIndex 的 upsert、delete、refresh、commit、search。14 项测试在类上使用 `@Disabled`，完成后移除。solution 启用全部测试，使用真实 Lucene 内存/文件索引，没有模拟搜索结果。

## 验证

```bash
mvn -B -pl exercises/search-basics/starter -am test
mvn -B -pl exercises/search-basics/solution -am verify
```

预期 starter 编译并跳过 14 项测试，solution 通过 14 项。测试覆盖空索引、英文大小写、跨标题/描述的 AND 查询、中文双字分析、精确级别过滤、普通文本而非查询语法、更新、删除、刷新、提交、文件重开、未提交变更丢弃、1100 条精确计数、输入边界和资源所有权。

## 演示

在仓库根目录按顺序执行：

```bash
mvn -B -pl exercises/search-basics/solution -am compile
mvn -B -f exercises/search-basics/solution/pom.xml dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency
java -cp 'exercises/search-basics/solution/target/classes:exercises/search-basics/solution/target/dependency/*' io.github.javamastery.exercises.search.SearchBasicsDemo
```

Windows 请调整 classpath 分隔符。关键输出：

```text
Before refresh: 0
After refresh: 2
Chinese cache matches: 1
Beginner matches: 1
Reopened matches: 2
```

示例创建自己的临时索引目录，显式提交后关闭重开，最后仅删除自己的目录。不是进程崩溃或掉电测试。

## API 约定

CourseDocument 校验正数 ID、非空标题（最多 200 字符）、非空描述（最多 4000 字符）和级别。标题与描述合并为 content 全文字段，原文另存为 StoredField；ID 和级别是未分词的精确字段。原文存储不等于可全文查询。

search 接受普通文本，不解析字段、通配符或布尔操作符语法。分析后所有词项必须出现，允许分布在标题和描述中，不要求短语顺序；level 为 null 表示不过滤。关键词最多 200 字符、limit 为 1 到 50。空白输入报错，分析后没有词项则返回空结果，不自动变成全量查询。

返回按引擎相关性排列的 Top-N 与当前快照精确总数；总数使用同一 IndexSearcher 的 count 取得，代价高于只取 Top-N。相同分数不承诺稳定业务排序。CJKAnalyzer 使用双字组合，不是中文语义分词、拼音或任意子串匹配。

Directory 由调用方持有，先创建 Directory，再创建 CourseSearchIndex，以 try-with-resources 反向关闭。索引对象拥有 writer、searcher manager 和 analyzer，但不关闭 Directory。一个目录同时只允许一个写入器；本封装同步公开方法，不作为高吞吐并发搜索服务。

写入、更新、删除不会自动刷新。refresh 更新查询快照，但不提交；commit 写入提交点，但不自动刷新已持有的查询快照。本封装显式关闭 commitOnClose，close 会丢弃最近提交之后的变更，使用者必须主动 commit。

## 边界与资料

这是嵌入式单机索引，不是分布式搜索服务，也不是业务数据库。没有数据库到索引的自动同步、权限过滤、分页游标、拼写纠正或自动重建；需另外设计来源、版本、重建、保留、权限和故障恢复。来自外部的不可信索引不能直接当本示例格式打开，索引损坏和 I/O 错误直接抛出。

- [Lucene 10.3.1 系统要求](https://lucene.apache.org/core/10_3_1/SYSTEM_REQUIREMENTS.html)
- [CJKAnalyzer](https://lucene.apache.org/core/10_3_1/analysis/common/org/apache/lucene/analysis/cjk/CJKAnalyzer.html)
- [QueryBuilder](https://lucene.apache.org/core/10_3_1/core/org/apache/lucene/util/QueryBuilder.html)
- [SearcherManager](https://lucene.apache.org/core/10_3_1/core/org/apache/lucene/search/SearcherManager.html)

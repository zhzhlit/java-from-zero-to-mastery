# 面向对象练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，只提供可编译的 API 骨架。
- `solution`：参考实现与完整自动化测试。

这组练习也是 Java 核心阶段综合练习。它使用“课程学习”业务背景，把类与对象、封装、继承、多态、接口、异常、集合、泛型、文件 I/O、日期时间、Stream、枚举和常用标准库串成一个可验证的小模型。

建议先阅读课程的[Java 核心阶段复盘](../../docs/zh-CN/guide/02-java-core/12-java-core-review.md)，再按下面的检查点完成 `starter`。完成后对照 `solution`，重点比较对象边界、异常路径、集合返回和测试覆盖，而不是直接复制答案。

验证 starter 可以编译：

```bash
mvn -B -pl exercises/object-oriented/starter -am compile
```

验证参考答案：

```bash
mvn -B -pl exercises/object-oriented/solution -am test
```

## 练习

1. `Lesson`：使用字段、构造器和 getter 保存课时标题与时长。
2. `CourseProgress`：组合一个 `Lesson` 对象，记录学习者完成的分钟数。
3. `StudyGoal`：封装学习目标，保证完成分钟数不会超过目标分钟数。
4. `LearningResource`、`VideoResource` 与 `ArticleResource`：练习继承、方法重写和父类引用多态调用。
5. `ReviewResult`、`QuizResult` 与 `CodeReviewResult`：练习接口、默认方法和接口引用多态调用。
6. `EnrollmentService` 与 `EnrollmentException`：练习参数校验、自定义异常和 `try/catch` 边界处理。
7. `CourseCatalog`：练习 `List`、`Set`、`Map`、标签索引和只读集合返回。
8. `LearningQueue<T>` 与 `ReviewResultSelector`：练习泛型类、泛型方法和类型参数上界。
9. `StudyReportFileStore`：练习 `Path`、`Files`、UTF-8 文本读写和 `IOException`。
10. `StudySchedule`：练习 `LocalDate`、`LocalTime`、`LocalDateTime`、`Duration` 和格式化输出。
11. `StudyStreamAnalyzer`：练习 lambda、Stream 筛选、转换、排序、统计和方法引用。
12. `StudyTaskStatus`、`StudyTaskPriority`、`StudyTask` 与 `StudyTaskBoard`：练习枚举、`Optional`、`Objects`、`StringJoiner`、`Comparator` 和 `EnumMap`。

## 知识点映射

| 知识点 | 对应练习 | 重点检查 |
| --- | --- | --- |
| 类与对象 | `Lesson`、`CourseProgress` | 字段、构造器、getter、固定格式摘要 |
| 封装 | `StudyGoal`、`CourseProgress` | 不变量、状态更新、边界值 |
| 继承与多态 | `LearningResource`、`VideoResource`、`ArticleResource` | 父类引用、多态调用、方法重写 |
| 接口 | `ReviewResult`、`QuizResult`、`CodeReviewResult` | 接口方法、默认方法、接口引用 |
| 异常 | `EnrollmentService`、`EnrollmentException` | 参数校验、业务异常、失败后状态 |
| 集合 | `CourseCatalog` | `List`、`Set`、`Map`、只读返回 |
| 泛型 | `LearningQueue<T>`、`ReviewResultSelector` | 泛型类、泛型方法、类型参数上界 |
| 文件 I/O | `StudyReportFileStore` | `Path`、`Files`、UTF-8、`IOException` |
| 日期时间 | `StudySchedule` | `LocalDateTime`、`Duration`、固定时间测试 |
| Lambda 与 Stream | `StudyStreamAnalyzer` | 筛选、映射、排序、统计、方法引用 |
| 枚举与标准库 | `StudyTaskStatus`、`StudyTaskBoard` | `enum`、`Optional`、`Objects`、`Comparator`、`EnumMap` |

## 推荐检查点

### 1. 基础对象

完成 `Lesson`、`CourseProgress` 和 `StudyGoal`。

- 构造器拒绝空标题、空学习者和非正数时长。
- 记录学习分钟数时不允许负数。
- 完成分钟数不会超过目标或课时时长。
- `summary` 输出稳定，方便测试和排查。

### 2. 抽象边界

完成学习资源、评审结果和报名服务。

- 子类能通过父类引用统一汇总预计学习时长。
- 不同评审结果能通过接口引用统一统计通过数量。
- 报名满员时抛出 `EnrollmentException`。
- 异常发生后报名人数不变。

### 3. 数据结构

完成课程目录、泛型学习队列和最佳评审选择。

- 课时顺序由 `List` 保持。
- 标签由 `Set` 去重。
- 标签索引用 `Map` 支持查询。
- 对外返回的集合不能被调用者修改内部状态。
- 泛型队列能复用在不同类型上。

### 4. 工程常用 API

完成学习报告、学习日程、Stream 分析和任务看板。

- 文件读写使用 UTF-8，并用临时目录测试。
- 时间计算使用固定日期时间测试。
- Stream 流水线每一步职责清楚。
- 任务状态和优先级用枚举表达。
- 查询可能为空时使用 `Optional`。
- 排序和统计规则有稳定测试覆盖。

## 建议顺序

1. 先为 `Lesson` 补充构造器校验，拒绝空标题和非正数时长。
2. 再在 `CourseProgress` 中保存学习者姓名与课时对象。
3. 实现 `recordStudyMinutes`，并确保完成分钟数不会超过课时时长。
4. 最后实现 `remainingMinutes`、`completionRate`、`isCompleted` 和固定格式 `summary`。
5. 实现 `StudyGoal`，练习用业务方法维护对象不变量。
6. 实现学习资源继承层次，并用 `LearningResource[]` 汇总不同子类的预计学习时长。
7. 实现评审结果接口，并用 `ReviewResult[]` 汇总不同实现类的通过数量和报告文本。
8. 实现课程报名服务，确保满员报名抛出 `EnrollmentException`，且失败后报名人数不变。
9. 实现课程目录，用 `List` 保存课时顺序，用 `Set` 去重标签，用 `Map` 支持按标签查询。
10. 实现泛型学习队列，并用 `<T extends ReviewResult>` 选择最佳评审结果。
11. 实现学习报告文件存储，用 `@TempDir` 测试保存、追加和读取。
12. 实现学习日程，用固定日期时间测试开始时间、结束时间和日历行。
13. 实现 Stream 分析器，用 `filter`、`map`、`sorted` 和 `mapToInt` 处理课程与评审结果。
14. 实现学习任务看板，用枚举建模状态和优先级，并用常用标准库完成查询、排序和统计。

## 完成标准

完成本练习后，你应该能做到：

- 解释每个类负责保存什么状态、保护什么规则。
- 为成功路径和失败路径各写出至少一个测试。
- 说明为什么某处使用继承，某处使用接口。
- 说明 `List`、`Set`、`Map` 和泛型在练习中的职责。
- 读懂并修改文件 I/O、时间、Stream、枚举和常用标准库相关代码。
- 看到测试失败时，能根据失败消息定位到对象规则、集合语义或边界处理。

## 复盘问题

1. 哪些规则必须放在构造器里？哪些规则更适合放在业务方法里？
2. 如果新增一种学习资源，你需要改哪些代码？这能说明多态边界是否清楚吗？
3. 如果新增一种评审结果，实现接口比继承父类有什么不同？
4. 哪些测试验证了失败后状态不变？
5. `CourseCatalog` 为什么同时需要 `List`、`Set` 和 `Map`？
6. `LearningQueue<T>` 的类型参数让哪些逻辑可以复用？
7. 文件 I/O 测试为什么使用临时目录？
8. 时间相关测试为什么不直接使用当前时间？
9. `StudyStreamAnalyzer` 中哪一步最适合改回普通循环？为什么？
10. `StudyTaskBoard` 中哪些地方如果改成字符串或 `null` 会更容易出错？

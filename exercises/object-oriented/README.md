# 面向对象练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，只提供可编译的 API 骨架。
- `solution`：参考实现与完整自动化测试。

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

建议按以下顺序完成：

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

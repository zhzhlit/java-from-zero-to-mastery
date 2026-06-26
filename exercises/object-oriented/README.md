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

建议按以下顺序完成：

1. 先为 `Lesson` 补充构造器校验，拒绝空标题和非正数时长。
2. 再在 `CourseProgress` 中保存学习者姓名与课时对象。
3. 实现 `recordStudyMinutes`，并确保完成分钟数不会超过课时时长。
4. 最后实现 `remainingMinutes`、`completionRate`、`isCompleted` 和固定格式 `summary`。

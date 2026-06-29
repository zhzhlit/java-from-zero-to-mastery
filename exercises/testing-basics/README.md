# JUnit 5 专项练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现业务代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整 JUnit 5 自动化测试。

请先阅读课程的 [JUnit 5 专项练习](../../docs/zh-CN/guide/03-engineering-foundation/02-junit5-testing-basics.md)，再进入 `starter`。练习时建议一次只移除一个测试方法上的 `@Disabled`，运行测试看到失败，再补实现或断言。

验证 starter 可以编译并运行测试骨架：

```bash
mvn -B -pl exercises/testing-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/testing-basics/solution -am test
```

## 练习

1. `StudySessionTest`：练习基础断言、`assertAll`、异常测试和 25 分钟边界。
2. `ScoreGradeTest`：练习 `@ParameterizedTest`、`@CsvSource`、`@ValueSource` 和分数边界。
3. `LearningPlanTest`：练习集合结果、`Optional` 空值边界和大小写无关查询。

## 推荐顺序

1. 先打开 `StudySessionTest`，移除第一个测试上的 `@Disabled`，补全业务实现直到测试通过。
2. 再处理异常测试，确保空主题和非正数分钟数抛出 `IllegalArgumentException`。
3. 使用参数化测试覆盖 24、25、50 分钟三个边界。
4. 完成 `ScoreGradeTest`，注意 59/60 和 89/90 这两组临界点。
5. 完成 `LearningPlanTest`，重点观察 `Optional` 如何表达“没有结果”。

## 完成标准

完成本练习后，你应该能做到：

- 用清晰测试名称描述被验证的行为。
- 用 `assertEquals`、`assertTrue`、`assertFalse` 和 `assertAll` 写基础断言。
- 用 `assertThrows` 验证失败路径。
- 用参数化测试减少重复，并覆盖关键边界值。
- 解释为什么空集合、临界分数和非法输入都需要测试。

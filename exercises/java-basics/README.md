# Java 基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，只提供可编译的 API 骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读题目并修改 `starter`，完成后再对照 `solution`。不要直接复制答案；先让测试或示例暴露自己的理解偏差。

验证 starter 可以编译：

```bash
mvn -B -pl exercises/java-basics/starter -am compile
```

验证参考答案：

```bash
mvn -B -pl exercises/java-basics/solution -am test
```

## 练习

1. `BmiCalculator`：使用变量、基本类型和算术运算计算 BMI。
2. `TicketPrice`：使用分支和边界条件计算不同年龄的票价。
3. `TimeFormatter`：通过方法拆分、除法和取余格式化秒数。
4. `ScoreParser`：把逗号分隔的文本解析为合法成绩数组。
5. `ScoreStatistics`：计算总分、平均分、最高分、最低分和达标人数。
6. `ScoreReport`：使用 `StringBuilder` 生成固定格式的成绩报告。

成绩统计器建议按以下顺序完成：

1. 先实现并手工验证 `ScoreParser`。
2. 再实现每个独立统计方法。
3. 最后组合 `ScoreReport`。
4. 遇到数组越界或结果不符时，在循环条件处设置断点，观察 `index`、当前元素和累计值。

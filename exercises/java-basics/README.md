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

# 数据结构基础练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的[数据结构基础](../../docs/zh-CN/guide/03-engineering-foundation/03-data-structures-basics.md)，再进入 `starter`。练习时建议一次只移除一个测试方法上的 `@Disabled`，运行测试看到失败，再补实现。

验证 starter：

```bash
mvn -B -pl exercises/data-structures-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/data-structures-basics/solution -am test
```

## 练习

1. `DynamicIntArray`：实现可扩容的 `int` 数组，练习索引访问、扩容、删除和边界校验。
2. `LinkedStudyList`：实现单向链表，练习头尾添加、删除头节点和顺序遍历。
3. `StudyStack`：实现后进先出结构，练习 `push`、`pop`、`peek` 和空栈边界。
4. `StudyQueue`：实现先进先出结构，练习 `offer`、`poll`、`peek` 和空队列边界。
5. `SimpleStringIntMap`：实现简化 key/value 表，练习保存、覆盖、读取、删除和 key 集合。

## 推荐顺序

1. 先完成 `DynamicIntArray` 的追加和读取，再做扩容和删除。
2. 再完成 `LinkedStudyList`，体会数组移动和链表改引用的不同。
3. 完成 `StudyStack` 和 `StudyQueue`，对比后进先出与先进先出。
4. 最后完成 `SimpleStringIntMap`，理解 key 查找、覆盖和删除语义。

## 完成标准

完成本练习后，你应该能做到：

- 解释数组、链表、栈、队列和 Map 分别解决什么问题。
- 说明动态数组扩容时为什么要复制旧数据。
- 说明链表删除头节点时需要更新哪些引用。
- 区分 `pop`、`poll`、`peek` 的行为差异。
- 用测试覆盖空结构、越界索引、重复 key 和删除后读取等边界。

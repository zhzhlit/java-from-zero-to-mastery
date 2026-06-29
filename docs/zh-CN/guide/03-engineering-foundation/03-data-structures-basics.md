---
title: 数据结构基础
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-29"
---

# 数据结构基础

## 学习目标

- 理解数据结构要解决的是“数据如何组织”和“操作如何高效”。
- 读懂数组、链表、栈、队列和 Map 的基本行为。
- 用 Big O 描述常见操作的时间复杂度。
- 通过 JUnit 5 测试验证空结构、越界、扩容、删除和重复 key 等边界。

## 前置知识

已完成 [JUnit 5 专项练习](./02-junit5-testing-basics.md)，能够阅读测试失败信息，并能用 `assertEquals`、`assertThrows`、`Optional` 和参数化思维验证边界条件。

## 练习入口

本章对应仓库中的数据结构基础练习：

- [`exercises/data-structures-basics/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/data-structures-basics/starter)：学习者编辑区。
- [`exercises/data-structures-basics/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/data-structures-basics/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/data-structures-basics/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/data-structures-basics/solution -am test
```

## 数据结构学什么

数据结构不是背 API 名字，而是回答三个问题：

1. 数据放在哪里？
2. 查找、插入、删除需要多少成本？
3. 当前业务更重视哪一种操作？

例如课程学习系统中：

- 学习章节顺序适合用列表保存。
- 最近打开的章节可以用栈表达。
- 待复习任务可以用队列表达。
- 课程编号到课程信息的查询适合用 Map 表达。

## Big O 入门

Big O 描述输入规模变大时，操作成本如何增长。

| 表达 | 含义 | 例子 |
| --- | --- | --- |
| `O(1)` | 常数时间 | 数组按索引读取、栈顶查看 |
| `O(n)` | 线性时间 | 遍历列表、链表按索引查找 |
| `O(log n)` | 对数时间 | 二分查找、平衡树查找 |
| `O(n log n)` | 线性对数时间 | 常见高效排序 |

初学阶段不需要追求复杂证明，但要形成直觉：当数据量从 10 变成 10000 时，线性扫描和直接定位的差距会非常明显。

## 动态数组

数组按索引读取很快，但固定长度。动态数组的做法是：

1. 先准备一个内部数组。
2. 追加元素时写入末尾。
3. 空间不够时创建更大的数组。
4. 把旧数据复制到新数组。

本章练习的 `DynamicIntArray` 会覆盖：

- `add` 追加元素。
- `get` 按索引读取。
- `removeAt` 删除并左移后续元素。
- `capacity` 观察扩容。
- 越界索引抛出 `IndexOutOfBoundsException`。

## 链表

链表用节点保存数据和下一个节点引用。它不需要整体搬移数组元素，但按索引查找需要从头开始走。

本章的 `LinkedStudyList` 使用单向链表，重点不是写出完整 `LinkedList`，而是理解：

- 头节点和尾节点分别是什么。
- 从头部添加时如何更新 `head`。
- 从尾部添加时如何更新 `tail`。
- 删除头节点时如何处理空链表。

## 栈和队列

栈和队列都是限制访问方式的线性结构：

- 栈：后进先出，适合撤销、调用栈、最近访问记录。
- 队列：先进先出，适合任务排队、消息处理、学习计划待办。

练习中的 `StudyStack` 和 `StudyQueue` 都有：

- 写入操作：`push` 或 `offer`。
- 取出操作：`pop` 或 `poll`。
- 查看但不移除：`peek`。
- 空结构边界：空栈弹出、空队列出队应抛出异常。

## Map 思维

Map 表达 key 到 value 的映射。它适合“我知道编号或名字，要快速找到对应数据”的场景。

本章的 `SimpleStringIntMap` 不要求手写完整哈希表，而是先练习 Map 语义：

- 新 key 保存值。
- 重复 key 覆盖旧值，并返回旧值。
- 删除 key 后不应继续读到值。
- 返回当前所有 key。

真正的哈希函数、冲突处理和扩容会放在后续算法与集合深入主题中展开。

## 分级练习

- **基础**：完成 `DynamicIntArray`，重点关注扩容和索引边界。
- **进阶**：完成 `LinkedStudyList`，画出每次添加和删除后 `head`、`tail` 的指向。
- **挑战**：完成 `SimpleStringIntMap`，解释覆盖旧值和删除 key 后读取为空的测试为什么必要。

## 常见错误与排查

- 扩容后忘记复制旧数据：前面的元素会丢失。
- 删除数组元素后忘记左移：读取顺序会错。
- 链表删除最后一个节点后忘记清空 `tail`：后续添加可能接到旧节点。
- `peek` 写成了移除元素：测试中 `size` 会变小。
- 重复 key 时直接新增记录：`size` 会错误增加。

## 面试与复习题

1. 动态数组为什么需要扩容？
2. 数组删除中间元素为什么通常是 `O(n)`？
3. 链表按索引读取为什么通常是 `O(n)`？
4. 栈和队列的访问规则分别是什么？
5. `peek` 和 `pop` / `poll` 的区别是什么？
6. Map 为什么适合按 key 查询？
7. 重复 key 覆盖旧值时，`size` 应该变化吗？

## 本章总结

数据结构让你开始从“代码能写出来”走向“代码为什么这样组织”。数组、链表、栈、队列和 Map 是后续算法、数据库索引、缓存、消息队列和 Web 后端设计的基础。

## 下一步

上一章：[JUnit 5 专项练习](./02-junit5-testing-basics.md)

下一章：[算法入门](./04-algorithms-basics.md)

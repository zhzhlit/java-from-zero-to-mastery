# 算法入门练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的[算法入门](../../docs/zh-CN/guide/03-engineering-foundation/04-algorithms-basics.md)，再进入 `starter`。练习时建议一次只移除一个测试方法上的 `@Disabled`，运行测试看到失败，再补实现。

验证 starter：

```bash
mvn -B -pl exercises/algorithms-basics/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/algorithms-basics/solution -am test
```

## 练习

1. `LinearSearch`：实现顺序查找，覆盖找到、找不到、重复值和空数组。
2. `BinarySearch`：实现有序数组二分查找，练习左右边界和找不到目标值。
3. `SelectionSort`：实现选择排序，练习每轮选择最小值并交换。
4. `InsertionSort`：实现插入排序，练习局部有序和元素右移。
5. `ComplexityExplorer`：用计数方法观察 `O(1)`、`O(n)` 和 `O(n^2)` 的增长差异。

## 推荐顺序

1. 先完成 `LinearSearch`，理解最直接的遍历。
2. 再完成 `BinarySearch`，注意它要求输入已经有序。
3. 完成 `SelectionSort` 和 `InsertionSort`，对比两种简单排序的思路。
4. 最后完成 `ComplexityExplorer`，用测试数据解释复杂度增长。

## 完成标准

完成本练习后，你应该能做到：

- 解释顺序查找和二分查找的适用前提。
- 说明选择排序每轮如何确定最小值。
- 说明插入排序为什么适合小规模或近似有序数据。
- 用简单例子区分 `O(1)`、`O(n)` 和 `O(n^2)`。
- 用测试覆盖空数组、边界索引、重复值和输入不被修改等行为。

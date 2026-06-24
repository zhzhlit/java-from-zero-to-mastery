# 贡献指南

感谢你帮助完善 Java 从入门到精通。

## 开始之前

- 小型勘误可以直接提交 Pull Request。
- 新课程、新项目或较大重构请先创建 Issue，说明受众、学习目标、范围和验收方式。
- 安全问题请勿公开利用细节；请通过 GitHub Security Advisory 私下报告。

## 内容贡献

正式课程应遵循 `docs/zh-CN/writing-guide.md`，并包含：

- `status`、`javaVersion`、`verifiedAt` frontmatter
- 清晰的前置知识和学习目标
- 可运行示例或可验证说明
- 基础、进阶、挑战三级练习
- 常见错误、边界和复习题

只有内容完整时才能标记为 `learning-ready`；只有经过版本复核和自动验证时才能标记为 `verified`。

## 代码贡献

- 以 Java 21 为基线。
- 示例保持聚焦，不引入与知识点无关的框架。
- 新行为先写失败测试，再实现最小代码。
- 每个模块必须提供运行命令、预期结果和必要测试。

## 本地验证

提交前运行：

```bash
mvn -B verify
npm ci --prefix website
npm run check:links --prefix website
npm run docs:build --prefix website
```

## 分支与提交

- 从最新的 `main` 创建短生命周期分支。
- 一个提交解决一个清晰问题。
- 推荐提交前缀：`feat:`、`fix:`、`docs:`、`test:`、`build:`、`ci:`、`chore:`。
- 不要混入无关格式化或重构。

## Pull Request 检查

- [ ] 变更范围与 Issue 或说明一致
- [ ] Java 构建和测试通过
- [ ] 文档链接检查通过
- [ ] VitePress 构建通过
- [ ] 版本敏感内容标明适用版本和验证日期
- [ ] 没有提交密钥、令牌、个人数据或生成目录


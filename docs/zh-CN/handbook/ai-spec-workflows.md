---
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-07-23"
---

# AI 规格工作流选型：Spec Kit、OpenSpec 与 Superpowers

这三个工具都在解决“别让 AI 拿到一句模糊需求就直接乱写代码”，但定位不同：

| 工具 | 核心定位 | 更适合 |
|---|---|---|
| Spec Kit | 完整、阶段化的规格驱动开发 | 新项目、大功能、团队协作、需要严格审计 |
| OpenSpec | 轻量、增量式规格管理 | 已有项目、持续迭代、中小功能 |
| Superpowers | 约束 AI 的开发方法和技能集 | 头脑风暴、TDD、调试、代码审查、执行计划 |

最重要的一点：**Spec Kit 和 OpenSpec 通常二选一；Superpowers 可以叠加在其中一个之上。**

## 1. Spec Kit 怎么用

Spec Kit 是 GitHub 推出的重型 SDD 工作流，主线是：

```text
项目原则 → 功能规格 → 技术计划 → 任务拆分 → 一致性检查 → 实现
```

### 安装

需要先安装 `uv`，然后从官方仓库安装。官方建议指定最新 Release 标签：

```bash
uv tool install specify-cli \
  --from git+https://github.com/github/spec-kit.git@vX.Y.Z

specify version
```

不要直接安装 PyPI 上同名的 `specify-cli`，官方说明那个包并非 Spec Kit 官方维护。[官方安装说明](https://github.com/github/spec-kit/blob/main/docs/installation.md)

### 初始化 Codex 项目

新项目：

```bash
specify init my-project --integration codex
cd my-project
```

已有项目：

```bash
specify init --here --integration codex
```

如果希望使用 Codex skills 模式：

```bash
specify init --here \
  --integration codex \
  --integration-options="--skills"
```

### 标准工作流

在 Codex/AI 对话框依次运行：

```text
/speckit.constitution
```

定义项目不可违反的原则，例如：

```text
/speckit.constitution
使用 Java 21 和 Spring Boot；业务逻辑必须有单元测试；
禁止 Controller 直接访问 Repository；公共 API 必须保持向后兼容。
```

然后定义需求，只说“做什么、为什么”，暂时不要指定技术：

```text
/speckit.specify
为系统增加密码重置功能。用户通过邮件获取一次性链接；
链接 30 分钟失效；使用后立即作废；不得泄露邮箱是否注册。
```

澄清模糊点：

```text
/speckit.clarify
```

制定技术方案：

```text
/speckit.plan
使用 Spring Boot、PostgreSQL 和现有邮件服务；
令牌只保存 SHA-256 摘要；沿用当前分层架构。
```

生成任务并检查：

```text
/speckit.tasks
/speckit.analyze
```

确认规格、计划和任务没有矛盾后实施：

```text
/speckit.implement
```

在 Codex 的 skills 集成模式下，命令可能显示为 `$speckit-specify`、`$speckit-plan` 等，而不是斜杠命令。[官方 Codex 集成说明](https://github.com/github/spec-kit/blob/main/docs/reference/integrations.md)

### 什么时候选它

选择 Spec Kit，如果：

- 功能涉及多个模块或多个团队
- 需求需要正式评审
- 要保留完整的需求、设计和任务链路
- 是绿地项目或大规模重构

它的缺点是文件和流程相对多，小修小改容易显得过重。[官方工作流](https://github.com/github/spec-kit)

## 2. OpenSpec 怎么用

OpenSpec 更像“以变更为单位维护活规格”：

```text
提出变更 → 生成 proposal/spec/design/tasks → 实现 → 验证 → 合并并归档
```

它不会强迫你每次都走很重的阶段门，比较适合已有代码库。

### 安装

要求 Node.js 20.19 或更高版本：

```bash
node --version
npm install -g @fission-ai/openspec@latest
openspec --version
```

初始化并指定 Codex：

```bash
cd your-project
openspec init --tools codex
```

初始化后通常会创建：

```text
openspec/
├── specs/       # 当前系统正式规格
├── changes/     # 正在开发的变更
└── config.yaml
```

[官方安装与 CLI 文档](https://github.com/Fission-AI/OpenSpec/blob/main/docs/installation.md)

### 最短工作流

提出需求：

```text
/opsx:propose add-password-reset

增加密码重置功能：
- 链接有效期 30 分钟
- 只能使用一次
- 不泄露邮箱是否存在
- 需要补充接口测试
```

AI 会在 `openspec/changes/add-password-reset/` 下生成类似：

```text
proposal.md
design.md
tasks.md
specs/...        # 对正式规格的增量修改
```

先人工检查这些文件，然后执行：

```text
/opsx:apply add-password-reset
```

实现后建议验证：

```bash
openspec validate
openspec status --change add-password-reset
```

如果启用了扩展工作流，还可以使用：

```text
/opsx:verify add-password-reset
```

完成后：

```text
/opsx:sync add-password-reset
/opsx:archive add-password-reset
```

`sync` 把增量需求合并进正式规格，`archive` 保存本次变更的历史记录。[官方命令说明](https://github.com/Fission-AI/OpenSpec/blob/main/docs/commands.md)

### 常用辅助命令

```bash
openspec list
openspec view
openspec show add-password-reset
openspec validate
openspec update
```

升级 OpenSpec 后，应在项目内运行：

```bash
npm install -g @fission-ai/openspec@latest
openspec update
```

### 什么时候选它

选择 OpenSpec，如果：

- 项目已经存在
- 经常添加中小型功能
- 希望规格长期保留，但不想引入太多流程
- 需求实施过程中经常需要调整设计
- 团队成员使用不同的 AI 编程工具

## 3. Superpowers 怎么用

Superpowers 不是另一套规格目录，主要是一组强制 AI 遵守工程纪律的技能，例如：

- 先 brainstorming，再确定设计
- 先写实施计划
- 使用测试驱动开发
- 系统化调试，不凭感觉乱改
- 实施后进行代码审查
- 完成分支前运行验证

### 在 Codex App 安装

根据官方说明：

1. 打开 Codex 左侧的 **Plugins**
2. 找到 Coding 分类里的 **Superpowers**
3. 点击 `+` 安装
4. 重新打开任务或项目

[Superpowers 官方仓库与安装说明](https://github.com/obra/superpowers)

### 实际使用

安装后通常不需要记一堆 CLI 命令，直接在任务里明确要求它使用相应技能：

```text
使用 Superpowers 的 brainstorming，帮我把密码重置需求和边界条件梳理清楚。
```

```text
使用 writing-plans，根据已确认的规格制定实现计划。
```

```text
按照 test-driven-development 实现第一个任务。
```

```text
使用 systematic-debugging 分析这个失败测试，不要在确认根因前修改代码。
```

```text
实现完成后进行 code review，并在宣称完成前运行验证。
```

Superpowers 的价值不是生成更多规格文件，而是让 AI 在实现过程中遵守可靠的软件工程方法。[官方项目说明](https://github.com/obra/superpowers)

## 推荐组合

对多数个人开发者和中小团队，我推荐：

```text
OpenSpec 管“做什么”
        +
Superpowers 管“怎么做”
        +
Git 管“做过什么”
```

具体流程：

```text
/opsx:propose
→ 人工审核 proposal/spec/design/tasks
→ Superpowers 执行 brainstorming 或计划审查
→ /opsx:apply
→ Superpowers 按 TDD 实现、系统化调试、代码审查
→ /opsx:verify
→ /opsx:sync
→ /opsx:archive
```

如果是大型绿地项目，则换成：

```text
Spec Kit 管需求和计划
        +
Superpowers 管实现纪律
```

组合时要明确告诉 AI：

```text
Spec Kit/OpenSpec 生成的规格是需求事实来源。
Superpowers 只负责澄清、计划、TDD、调试和审查，
不要另外创建一套相互冲突的需求文档。
```

一句话选择建议：

- 小改动：直接编码或只用 Superpowers
- 已有项目持续迭代：OpenSpec + Superpowers
- 大功能、正式项目、强流程团队：Spec Kit + Superpowers
- 不建议：同一项目同时用 Spec Kit 和 OpenSpec 管同一批需求

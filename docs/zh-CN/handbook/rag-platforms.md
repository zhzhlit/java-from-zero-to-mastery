---
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-08-04"
---

# RAG 工具与平台选型：Kapa.ai、Mastra、MimirQ 与 WeKnora

这几个项目都和“让 AI 基于指定资料回答问题”有关，底层常见技术路线是 RAG（Retrieval-Augmented Generation，检索增强生成）：

```text
用户提问 → 检索知识库片段 → 把片段交给大模型 → 生成带依据的答案
```

它们解决的问题相近，但产品层级不同：有的是成品服务，有的是开发框架，有的是可治理的 RAG 流水线，也有完整的私有知识平台。

## 直观对比

| 项目 | 它是什么 | 主要用途 | 更适合谁 |
|---|---|---|---|
| [Kapa.ai](https://www.kapa.ai/) | 商业化、托管式 AI 知识助手 | 把产品文档、GitHub、Slack、PDF 等内容变成面向客户的问答助手 | 想快速上线、不想自己维护 RAG 的企业 |
| [Mastra RAG](https://mastra.ai/docs/rag/overview) | TypeScript AI 开发框架中的 RAG 能力 | 用代码自行构建 Agent、知识库问答和 AI 工作流 | TypeScript/Node.js 开发者 |
| [MimirQ](https://github.com/skygazer42/MimirQ) | 中文优先、开源、偏底层治理的企业 RAG 系统 | 精细控制文档解析、清洗、切块、检索、重排、引用和评测 | 对检索质量、审计和定制要求高的团队 |
| [WeKnora](https://github.com/Tencent/WeKnora/blob/main/README_CN.md) | 腾讯开源的一体化知识管理与 Agent 平台 | 建知识库、聊天、Agent、自动 Wiki、知识图谱和企业权限体系 | 想私有化部署完整知识平台的团队 |

## 1. Kapa.ai：开箱即用的产品文档客服

Kapa.ai 更像一个已经搭好的企业文档 AI 客服。你把内容源接进去，它负责索引、问答、引用、集成和分析。

常见接入内容包括：

- 产品文档网站
- GitHub 代码或 Issue
- PDF
- Slack、Discord
- Confluence、Notion、Jira、Zendesk 等系统

它适合技术产品、开发者工具和 SaaS 团队，用来在官网、文档站或客服入口回答用户问题。优点是上线快、运维轻；代价是商业服务依赖和深度定制空间有限。

**一句话：花钱买一个已经搭好并负责运维的企业文档 AI 客服。**

## 2. Mastra RAG：给开发者用的代码积木

Mastra 是一个开源 TypeScript AI 应用框架，RAG 只是其中一部分。它不是现成知识库产品，而是让开发者自己实现 AI 应用的框架能力。

典型流程是：

1. 读取文档
2. 把文档切成小块
3. 生成 Embedding 向量
4. 保存到 pgvector 等向量数据库
5. 提问时检索相关片段
6. 把片段提供给大模型回答

Mastra 提供文档处理、切块、Embedding、向量库和检索 API，但模型、数据库、界面、权限、部署方式仍需要自己设计。

**一句话：适合程序员自己写 TypeScript AI 产品。**

## 3. MimirQ：强调可控、可检查、可回归的 RAG 流水线

MimirQ 的重点不是“上传文件然后聊天”，而是解决企业 RAG 出错后难以定位的问题。

它更关注这些细节：

- PDF 表格是否解析丢失
- 文档清洗规则是否正确
- 切块是否破坏上下文
- 向量检索是否漏掉证据
- 重排模型是否把正确证据排到后面
- 新版本是否让原本正确的问题变差

因此它把链路拆得更细：

```text
数据评估 → 文档解析 → 清洗治理 → 业务切块 → 向量/全文索引 → 混合召回 → 重排与引用 → Golden 测试集回归
```

它适合把 RAG 当成工程系统认真治理的团队。能力更细，排查更透明，但部署和学习成本也更高。

**一句话：适合认真治理 RAG 质量、需要知道每一步为什么出错的团队。**

## 4. WeKnora：完整的私有知识库、Agent 和自动 Wiki 平台

WeKnora 更偏向一个可直接给员工或客户使用的完整知识平台。

它主要覆盖三类模式：

- **RAG 快速问答**：根据企业资料直接回答
- **ReAct Agent**：组合知识检索、网络搜索和 MCP 工具执行多步任务
- **Wiki 模式**：把原始文档自动整理成互相链接的 Markdown Wiki 和知识图谱

它还包含企业产品常见能力：

- PDF、Word、Excel、PPT、图片等文档导入
- 飞书、Notion、语雀、RSS 同步
- BM25、向量检索、GraphRAG、父子分块
- 多模型接入
- 多工作区、RBAC、审计日志
- 企业微信、飞书、Slack、Telegram 等渠道
- 网站嵌入 Widget
- MCP 工具
- 本地或私有云部署

它适合想把知识库、聊天、Agent、Wiki、权限和多渠道接入放在一个平台里统一建设的团队。

**一句话：更像一个可以私有部署的企业版 ChatGPT 知识平台。**

## 应该怎么选

- **最快上线官网文档助手**：Kapa.ai
- **自己写 TypeScript AI 产品**：Mastra
- **重点解决复杂文档、检索质量、审计与回归测试**：MimirQ
- **需要完整私有知识库、聊天、Agent、Wiki、权限和多渠道接入**：WeKnora

它们也可以组合使用：

- 用 MimirQ 管理和检索知识，再接到 Dify 或自研应用
- 用 Mastra 编写 Agent，再连接自己的知识库
- 用 Kapa 直接为产品官网和其他 Agent 提供知识
- 用 WeKnora 一体化建设公司内部知识平台

最简化地说：**Kapa 是成品服务，Mastra 是开发框架，MimirQ 是可治理的 RAG 知识流水线，WeKnora 是一体化知识与 Agent 平台。**

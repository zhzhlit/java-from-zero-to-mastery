# 贯穿式主项目

本目录用于承载“在线学习与知识社区”主项目的业务实现。项目会随课程阶段逐步演进，从命令行课程管理开始，逐步扩展到 Spring Boot、前后端分离、缓存、搜索、消息队列、微服务、云原生和 AI 学习助手。

完整路线见：[在线学习与知识社区主项目路线](../docs/zh-CN/projects/flagship-roadmap.md)。

## v0.4.0 验证入口

v0.4.0 不扩展主项目业务功能，重点是把命令行课程管理作为工程化验证样例。学习者应能够运行模块级测试、全仓验证、文档链接检查和文档构建，并理解这些命令与 CI 的关系。

推荐验证命令：

```bash
mvn -B -pl flagship-project/course-manager-cli -am test
mvn -B verify
npm run check:links --prefix website
npm run docs:build --prefix website
```

## v0.3.11 范围

v0.3.11 提供第一步可运行实现：

```text
flagship-project/course-manager-cli
```

它使用 Java 21、Maven 和 JUnit 5 实现命令行课程管理的最小业务闭环：

- 示例课程与课时建模。
- 课程编号查询、标签查询和阶段分组。
- 学习进度记录和完成百分比。
- 本地 `properties` 文件保存与读取。
- 命令行入口与自动化测试。

从仓库根目录验证：

```bash
mvn -B -pl flagship-project/course-manager-cli -am test
java -cp flagship-project/course-manager-cli/target/classes io.github.javamastery.flagship.cli.CourseManagerApp
```

当前仍暂不提供：

- 数据库脚本
- 前端页面
- Docker 或 Kubernetes 部署文件

后续版本应按路线图逐阶段补充数据库、Web、前端和部署实现，并在每个阶段提供独立的运行说明、测试命令和验收标准。

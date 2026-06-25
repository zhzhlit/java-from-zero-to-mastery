# Java 从入门到精通

一套面向大学生、Java 初级开发者和中级开发者的开源学习项目，以 Java 21 为基线，提供系统课程、工程手册和递进式实战。

> 当前学习版本：v0.2.2。已经提供从第一个程序到数组、字符串与调试的连续入门路线，以及成绩统计器 starter/solution 练习。

## 三条学习主线

- **系统课程**：按照明确路线从开发环境、Java 基础逐步走向企业开发、JVM、分布式和云原生。
- **精选手册**：面向日常开发快速查阅 API、实践边界和排查方法。
- **实战项目**：使用独立小项目训练单项能力，并通过“在线学习与知识社区”持续演进综合工程能力。

## 技术基线

- Java 21 LTS
- Maven 3.9+
- JUnit 5
- Spring Boot 3（后续课程）
- VitePress 1.6
- GitHub Actions 与 GitHub Pages

## 快速开始

验证全部 Java 示例和练习：

```bash
mvn -B verify
```

运行第一个示例：

```bash
mvn -B -pl examples/first-java-program -am clean test
java -cp examples/first-java-program/target/classes io.github.javamastery.examples.HelloJava
```

验证 Java 基础示例与练习：

```bash
mvn -B -pl examples/java-basics -am test
mvn -B -pl exercises/java-basics/starter -am compile
mvn -B -pl exercises/java-basics/solution -am test
```

启动文档站：

```bash
npm install --prefix website
npm run docs:dev --prefix website
```

构建并检查文档：

```bash
npm run check:links --prefix website
npm run docs:build --prefix website
```

## 从这里开始

- [学习路线](docs/zh-CN/roadmap/index.md)
- [第一个 Java 程序](docs/zh-CN/guide/01-getting-started/01-first-java-program.md)
- [开发环境与 IntelliJ IDEA](docs/zh-CN/guide/01-getting-started/02-development-environment-and-idea.md)
- [变量与数据类型](docs/zh-CN/guide/01-getting-started/03-variables-and-data-types.md)
- [流程控制](docs/zh-CN/guide/01-getting-started/04-control-flow.md)
- [方法](docs/zh-CN/guide/01-getting-started/05-methods.md)
- [数组](docs/zh-CN/guide/01-getting-started/06-arrays.md)
- [字符串与 StringBuilder](docs/zh-CN/guide/01-getting-started/07-strings-and-stringbuilder.md)
- [IntelliJ IDEA 调试](docs/zh-CN/guide/01-getting-started/08-debugging-with-intellij-idea.md)
- [成绩统计器练习](exercises/java-basics/README.md)
- [写作规范](docs/zh-CN/writing-guide.md)
- [主项目演进路线](docs/zh-CN/projects/flagship-roadmap.md)
- [贡献指南](CONTRIBUTING.md)

## 仓库结构

```text
docs/               课程、手册、路线和项目文档
examples/           可独立运行的知识点示例
exercises/          带自动测试的分级练习
mini-projects/      阶段性小项目
flagship-project/   贯穿式主项目
website/            VitePress 文档站
```

## 内容状态

- `planned`：已规划，尚不可学习
- `draft`：编写中，内容可能变化
- `learning-ready`：内容完整，可用于学习
- `verified`：已在声明的技术版本上复核和验证

默认学习导航只展示 `learning-ready` 和 `verified` 内容。

## 参与贡献

欢迎提交勘误、内容建议、练习、示例和项目改进。较大的新增主题请先创建 Issue，确认范围后再实现。提交前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

- 代码及构建配置采用 [Apache License 2.0](LICENSE-CODE)。
- 原创文档和图示采用 [CC BY 4.0](LICENSE-DOCS)。
- 第三方素材遵循其各自声明的许可证。

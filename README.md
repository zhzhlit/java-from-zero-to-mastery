# Java 从入门到精通

一套面向大学生、Java 初级开发者和中级开发者的开源学习项目，以 Java 21 为基线，提供系统课程、工程手册和递进式实战。

> 当前学习版本：v0.5.1。已经提供从第一个程序到 Spring Boot 配置的连续学习路线，以及成绩统计器、Java 核心综合练习、JUnit 5 专项练习、数据结构基础练习、算法入门练习、网络基础练习、操作系统基础练习、数据库基础练习、JDBC 基础练习、HTTP 基础练习、Servlet 基础练习、Web 基础综合练习、Spring Boot 基础练习、Spring Boot 配置练习和主项目命令行课程管理入口。

## 三条学习主线

- **系统课程**：按照明确路线从开发环境、Java 基础逐步走向企业开发、JVM、分布式和云原生。
- **精选手册**：面向日常开发快速查阅 API、实践边界和排查方法。
- **实战项目**：使用独立小项目训练单项能力，并通过“在线学习与知识社区”持续演进综合工程能力。

## 技术基线

- Java 21 LTS
- Maven 3.9+
- JUnit 5
- Spring Boot 3
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

验证 Java 核心示例与面向对象练习：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

验证 JUnit 5 专项练习：

```bash
mvn -B -pl exercises/testing-basics/starter -am test
mvn -B -pl exercises/testing-basics/solution -am test
```

验证数据结构基础练习：

```bash
mvn -B -pl exercises/data-structures-basics/starter -am test
mvn -B -pl exercises/data-structures-basics/solution -am test
```

验证算法入门练习：

```bash
mvn -B -pl exercises/algorithms-basics/starter -am test
mvn -B -pl exercises/algorithms-basics/solution -am test
```

验证网络基础练习：

```bash
mvn -B -pl exercises/network-basics/starter -am test
mvn -B -pl exercises/network-basics/solution -am test
```

验证操作系统基础练习：

```bash
mvn -B -pl exercises/operating-system-basics/starter -am test
mvn -B -pl exercises/operating-system-basics/solution -am test
```

验证数据库基础练习：

```bash
mvn -B -pl exercises/database-basics/starter -am test
mvn -B -pl exercises/database-basics/solution -am test
```

验证 JDBC 基础练习：

```bash
mvn -B -pl exercises/jdbc-basics/starter -am test
mvn -B -pl exercises/jdbc-basics/solution -am test
```

验证 HTTP 基础练习：

```bash
mvn -B -pl exercises/http-basics/starter -am test
mvn -B -pl exercises/http-basics/solution -am test
```

验证 Servlet 基础练习：

```bash
mvn -B -pl exercises/servlet-basics/starter -am test
mvn -B -pl exercises/servlet-basics/solution -am test
```

验证 Web 基础综合练习：

```bash
mvn -B -pl exercises/web-basics-review/starter -am test
mvn -B -pl exercises/web-basics-review/solution -am test
```

验证 Spring Boot 基础练习：

```bash
mvn -B -pl exercises/spring-boot-basics/starter -am test
mvn -B -pl exercises/spring-boot-basics/solution -am test
```

验证 Spring Boot 配置练习：

```bash
mvn -B -pl exercises/spring-boot-configuration/starter -am test
mvn -B -pl exercises/spring-boot-configuration/solution -am test
```

验证主项目命令行课程管理：

```bash
mvn -B -pl flagship-project/course-manager-cli -am test
java -cp flagship-project/course-manager-cli/target/classes io.github.javamastery.flagship.cli.CourseManagerApp
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
- [类与对象](docs/zh-CN/guide/02-java-core/01-classes-and-objects.md)
- [封装](docs/zh-CN/guide/02-java-core/02-encapsulation.md)
- [继承与多态](docs/zh-CN/guide/02-java-core/03-inheritance-and-polymorphism.md)
- [接口](docs/zh-CN/guide/02-java-core/04-interfaces.md)
- [异常处理](docs/zh-CN/guide/02-java-core/05-exceptions.md)
- [集合](docs/zh-CN/guide/02-java-core/06-collections.md)
- [泛型](docs/zh-CN/guide/02-java-core/07-generics.md)
- [文件 I/O](docs/zh-CN/guide/02-java-core/08-file-io.md)
- [日期与时间 API](docs/zh-CN/guide/02-java-core/09-date-time-api.md)
- [Lambda 与 Stream API](docs/zh-CN/guide/02-java-core/10-lambda-streams.md)
- [枚举与常用标准库](docs/zh-CN/guide/02-java-core/11-enums-standard-library.md)
- [Java 核心综合复盘](docs/zh-CN/guide/02-java-core/12-java-core-review.md)
- [测试、Maven 与工程化验证](docs/zh-CN/guide/03-engineering-foundation/01-testing-maven-ci.md)
- [JUnit 5 专项练习](docs/zh-CN/guide/03-engineering-foundation/02-junit5-testing-basics.md)
- [数据结构基础](docs/zh-CN/guide/03-engineering-foundation/03-data-structures-basics.md)
- [算法入门](docs/zh-CN/guide/03-engineering-foundation/04-algorithms-basics.md)
- [网络基础](docs/zh-CN/guide/03-engineering-foundation/05-network-basics.md)
- [操作系统基础](docs/zh-CN/guide/03-engineering-foundation/06-operating-system-basics.md)
- [数据库基础](docs/zh-CN/guide/04-database-web/01-database-basics.md)
- [JDBC 基础](docs/zh-CN/guide/04-database-web/02-jdbc-basics.md)
- [HTTP 基础](docs/zh-CN/guide/04-database-web/03-http-basics.md)
- [Servlet 基础](docs/zh-CN/guide/04-database-web/04-servlet-basics.md)
- [Web 基础综合复盘](docs/zh-CN/guide/04-database-web/05-web-basics-review.md)
- [Spring Boot 基础](docs/zh-CN/guide/05-enterprise-development/01-spring-boot-basics.md)
- [Spring Boot 配置](docs/zh-CN/guide/05-enterprise-development/02-spring-boot-configuration.md)
- [成绩统计器练习](exercises/java-basics/README.md)
- [面向对象练习](exercises/object-oriented/README.md)
- [JUnit 5 专项练习模块](exercises/testing-basics/README.md)
- [数据结构基础练习](exercises/data-structures-basics/README.md)
- [算法入门练习](exercises/algorithms-basics/README.md)
- [网络基础练习](exercises/network-basics/README.md)
- [操作系统基础练习](exercises/operating-system-basics/README.md)
- [数据库基础练习](exercises/database-basics/README.md)
- [JDBC 基础练习](exercises/jdbc-basics/README.md)
- [HTTP 基础练习](exercises/http-basics/README.md)
- [Servlet 基础练习](exercises/servlet-basics/README.md)
- [Web 基础综合练习](exercises/web-basics-review/README.md)
- [Spring Boot 基础练习](exercises/spring-boot-basics/README.md)
- [Spring Boot 配置练习](exercises/spring-boot-configuration/README.md)
- [命令行课程管理主项目](flagship-project/README.md)
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

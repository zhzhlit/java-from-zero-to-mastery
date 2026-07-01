---
title: Spring Boot 配置
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-07-01"
---

# Spring Boot 配置

## 学习目标

- 理解 Spring Boot 配置文件的作用。
- 能使用 `application.properties` 管理可变化参数。
- 能使用 `@ConfigurationProperties` 把配置绑定为类型安全对象。
- 能为配置提供安全默认值。
- 能在 Service 中使用配置控制业务行为。

## 前置知识

已完成 [Spring Boot 基础](./01-spring-boot-basics.md)，知道启动类、Controller、Service 和 DTO 的职责。本章继续把“写死在代码里的参数”移到配置文件中。

## 练习入口

本章对应仓库中的 Spring Boot 配置练习：

- [`exercises/spring-boot-configuration/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-configuration/starter)：学习者编辑区。
- [`exercises/spring-boot-configuration/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-configuration/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/spring-boot-configuration/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/spring-boot-configuration/solution -am test
```

## 为什么需要配置

真实应用中，很多参数不应该写死在 Java 代码里，例如：

- 默认课程标题。
- 每页显示多少条数据。
- 哪些标签是首页推荐标签。
- 外部服务地址、超时时间和开关。

配置文件让这些参数可以随环境调整，而不需要重新改业务代码。

## application.properties

Spring Boot 默认会读取 `src/main/resources/application.properties`：

```properties
course.catalog.default-title=Spring Boot 课程目录
course.catalog.page-size=20
course.catalog.featured-tags=java,spring,backend
```

这里的 `course.catalog` 是一个配置前缀。它把同一组业务配置收在一起，便于阅读和维护。

## 类型安全配置

相比在各处使用字符串读取配置，更推荐把配置绑定成一个明确的 Java 类型：

```java
@ConfigurationProperties("course.catalog")
public record CourseCatalogProperties(String defaultTitle, int pageSize, List<String> featuredTags) {
}
```

这样做有三个好处：

- 字段含义集中在一个类里。
- Service 依赖的是业务配置对象，而不是散落的字符串 key。
- IDE 和编译器能帮助发现类型错误。

启动类需要启用配置属性扫描：

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringBootConfigurationApplication {
}
```

## 安全默认值

配置来自外部，可能为空或不合法。练习中的 `CourseCatalogProperties` 会处理这些情况：

- 标题为空时使用 `Java 学习路线`。
- `pageSize <= 0` 时使用 `10`。
- 标签列表为 `null` 时使用空列表。

默认值应该靠近配置对象，而不是散落在多个 Service 中。

## Service 使用配置

配置对象注入到 Service 后，就可以控制业务行为：

```java
public CourseSearchView buildSearchView(String keyword, int requestedPageSize) {
    String normalizedKeyword = keyword == null || keyword.isBlank()
            ? properties.defaultTitle()
            : keyword.trim();
    int pageSize = requestedPageSize <= 0
            ? properties.pageSize()
            : Math.min(requestedPageSize, properties.pageSize());
    return new CourseSearchView(normalizedKeyword, pageSize, properties.featuredTags());
}
```

这段逻辑表达了两个业务规则：

- 用户没有输入关键词时，使用默认课程标题。
- 用户请求的分页大小不能超过配置上限。

## 配置绑定测试

练习使用 Spring Boot 的 `Binder` 验证配置绑定，不需要启动完整应用：

```java
MockEnvironment environment = new MockEnvironment()
        .withProperty("course.catalog.default-title", "企业级 Java")
        .withProperty("course.catalog.page-size", "12");

CourseCatalogProperties properties = Binder.get(environment)
        .bind("course.catalog", CourseCatalogProperties.class)
        .get();
```

这种测试很轻，适合快速验证配置 key、类型和默认值。

## 分级练习

- **基础**：完成 `CourseCatalogProperties` 的默认值处理。
- **进阶**：完成 `CourseCatalogService` 的关键词、分页和特色标签逻辑。
- **挑战**：使用 `Binder` 写出配置绑定测试。

## 常见错误与排查

- 忘记 `@ConfigurationPropertiesScan`：配置对象没有被注册。
- 配置前缀拼错：绑定结果为空或字段是默认值。
- 把配置散落在多个类里：后期难以统一调整。
- 缺少默认值：本地可以运行，换环境后容易出错。
- 把配置对象设计成可变集合：业务代码可能意外修改配置。

## 面试与复习题

1. `application.properties` 通常放在哪里？
2. `@ConfigurationProperties` 解决了什么问题？
3. 为什么配置对象需要安全默认值？
4. 配置前缀应该如何命名？
5. Service 使用配置时，哪些逻辑仍然应该保留在业务代码里？
6. `Binder` 测试比完整启动应用测试轻在哪里？

## 本章总结

Spring Boot 配置的重点是把可变化参数从业务代码中分离出来，并通过类型安全对象传递给业务层。完成本章后，你已经能为应用建立清晰的配置边界。

## 下一步

上一章：[Spring Boot 基础](./01-spring-boot-basics.md)

继续按照[学习路线](../../roadmap/index.md)进入分层架构。

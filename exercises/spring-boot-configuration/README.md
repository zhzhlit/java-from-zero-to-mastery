# Spring Boot 配置练习

本目录使用 `starter` 与 `solution` 双模块：

- `starter`：学习者编辑区，提供待实现代码和带 `@Disabled` 的测试练习骨架。
- `solution`：参考实现与完整自动化测试。

请先阅读课程的 [Spring Boot 配置](../../docs/zh-CN/guide/05-enterprise-development/02-spring-boot-configuration.md)，再进入 `starter`。本练习聚焦 `application.properties`、`@ConfigurationProperties`、配置默认值和 Service 使用配置。

验证 starter：

```bash
mvn -B -pl exercises/spring-boot-configuration/starter -am test
```

验证参考答案：

```bash
mvn -B -pl exercises/spring-boot-configuration/solution -am test
```

## 练习

1. `application.properties`：配置课程目录标题、分页上限和特色标签。
2. `CourseCatalogProperties`：使用 `@ConfigurationProperties` 绑定配置。
3. `CourseCatalogService`：在业务逻辑中使用配置，处理默认关键词和分页上限。
4. `CourseCatalogPropertiesTest`：使用 Spring Boot `Binder` 验证配置绑定。
5. `CourseCatalogServiceTest`：用普通单元测试验证配置驱动的业务行为。

## 完成标准

完成本练习后，你应该能做到：

- 说明 Spring Boot 配置文件如何映射到 Java 对象。
- 使用类型安全配置替代散落的字符串配置读取。
- 为配置提供安全默认值。
- 在 Service 中使用配置控制业务行为。

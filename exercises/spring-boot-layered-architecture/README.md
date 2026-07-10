# Spring Boot 分层架构练习

本模块练习 Controller、Service、Repository、DTO 和实体之间的职责边界。

## 练习目标

- Controller 只处理 HTTP 请求和响应。
- Service 组织业务流程并放置业务规则。
- Repository 隔离数据访问，先用内存实现模拟持久化。
- 区分请求 DTO、响应 DTO 和内部实体。
- 认识 `@Transactional` 所表达的事务边界。

## 验证

```bash
mvn -B -pl exercises/spring-boot-layered-architecture/starter -am test
mvn -B -pl exercises/spring-boot-layered-architecture/solution -am test
```

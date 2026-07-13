---
title: Spring Boot 分层架构
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-07-10"
---

# Spring Boot 分层架构

## 学习目标

- 理解 Controller、Service、Repository 的职责边界。
- 能区分请求 DTO、响应 DTO 和内部实体。
- 能用 Repository 接口隔离数据访问实现。
- 能在 Service 中放置业务规则和流程编排。
- 理解 `@Transactional` 表达的是业务操作的事务边界。

## 前置知识

已完成 [Spring Boot REST 校验](./03-spring-boot-rest-validation.md)，知道如何处理 HTTP 请求、请求校验和统一错误响应。本章继续把代码拆成更清晰的业务层次。

## 练习入口

本章对应仓库中的 Spring Boot 分层架构练习：

- [`exercises/spring-boot-layered-architecture/starter`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-layered-architecture/starter)：学习者编辑区。
- [`exercises/spring-boot-layered-architecture/solution`](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/spring-boot-layered-architecture/solution)：参考实现和完整测试。

运行 starter：

```bash
mvn -B -pl exercises/spring-boot-layered-architecture/starter -am test
```

运行参考答案：

```bash
mvn -B -pl exercises/spring-boot-layered-architecture/solution -am test
```

## 三层职责

常见 Spring Boot 后端可以先按三层理解：

- **Controller**：接收 HTTP 请求，调用 Service，组织状态码和响应体。
- **Service**：表达业务流程和业务规则，例如创建课程、发布课程、拒绝重复标题。
- **Repository**：隔离数据访问细节，本章先用内存实现模拟数据库。

这种拆分能让每个类的修改理由更单一，也让测试更容易定位问题。

## DTO 与实体

请求 DTO 面向调用方输入：

```java
public record CreateCourseRequest(String title, int lessonCount) {
}
```

响应 DTO 面向调用方输出：

```java
public record CourseDto(long id, String title, int lessonCount, String status) {
}
```

内部实体表达业务状态和行为：

```java
public class Course {
    public void publish() {
        if (published()) {
            throw new BusinessRuleException("课程已经发布");
        }
        status = CourseStatus.PUBLISHED;
    }
}
```

不要把内部实体直接当作 API 响应，这会让外部接口被内部实现牵着走。

## Repository 接口

Repository 接口描述业务层需要的数据能力：

```java
public interface CourseRepository {
    List<Course> findAll();
    Optional<Course> findById(long id);
    boolean existsByTitleIgnoreCase(String title);
    Course save(Course course);
}
```

本章用 `InMemoryCourseRepository` 实现它。之后替换成数据库实现时，Service 的业务规则可以尽量保持稳定。

## Service 与事务边界

Service 负责组织一次完整业务操作：

```java
@Transactional
public CourseDto create(CreateCourseRequest request) {
    String title = normalizeTitle(request.title());
    if (courseRepository.existsByTitleIgnoreCase(title)) {
        throw new BusinessRuleException("课程标题不能重复");
    }
    Course course = new Course(courseRepository.nextId(), title, request.lessonCount(), CourseStatus.DRAFT);
    return CourseDto.from(courseRepository.save(course));
}
```

`@Transactional` 不是“让方法更高级”的注解，而是在表达：这段业务流程以后接入数据库时，应作为一个整体提交或回滚。

## 分级练习

- **基础**：完成 Repository 的标题重复检查。
- **进阶**：在 Service 中完成创建课程、发布课程和业务规则。
- **挑战**：保持 Controller 轻量，只让它处理 HTTP 边界。

## 常见错误与排查

- Controller 里堆业务规则：会让 HTTP 边界和业务流程混在一起。
- Service 直接依赖具体内存实现：后续替换数据库会更困难。
- API 响应直接返回实体：内部字段变化会影响外部接口。
- 忘记处理业务异常：调用方只能看到模糊的 500。
- 把 `@Transactional` 随处乱放：事务边界应该对应完整业务操作。

## 面试与复习题

1. Controller、Service、Repository 分别负责什么？
2. DTO 和实体为什么要拆开？
3. Repository 接口有什么价值？
4. 标题不能重复这样的规则应该放在哪一层？
5. `@Transactional` 适合标在什么类型的方法上？
6. 为什么发布课程不能只在 Controller 里改状态？

## 本章总结

分层架构的重点不是多写几个包，而是让 HTTP、业务流程和数据访问各自有清楚边界。完成本章后，你已经能把一个小型 Spring Boot API 拆成更容易演进和测试的结构。

## 下一步

上一章：[Spring Boot REST 校验](./03-spring-boot-rest-validation.md)

继续按照[学习路线](../../roadmap/index.md)进入真实数据库集成与事务实践。

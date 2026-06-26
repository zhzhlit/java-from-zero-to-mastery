package io.github.javamastery.flagship.cli;

import java.util.List;
import java.util.Set;

public final class SampleCourses {

    private SampleCourses() {
    }

    public static CourseCatalog coreReviewCatalog() {
        CourseCatalog catalog = new CourseCatalog();
        catalog.addCourse(javaBasicsCourse());
        catalog.addCourse(javaCoreReviewCourse());
        return catalog;
    }

    public static Course javaBasicsCourse() {
        return new Course(
                "JAVA-BASICS",
                "Java 基础回顾",
                CourseLevel.BASICS,
                List.of(
                        new Lesson("basics-variables", "变量、类型与控制流", 45),
                        new Lesson("basics-methods", "方法、数组与字符串", 60)),
                Set.of("java", "basics", "review"));
    }

    public static Course javaCoreReviewCourse() {
        return new Course(
                "JAVA-CORE",
                "Java 核心综合复盘",
                CourseLevel.CORE,
                List.of(
                        new Lesson("core-oop", "面向对象建模", 60),
                        new Lesson("core-collections", "集合、泛型与 Stream", 75),
                        new Lesson("core-io-time", "异常、文件与时间 API", 60)),
                Set.of("java", "core", "project"));
    }
}

---
title: 文件 I/O
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-06-26"
---

# 文件 I/O

## 学习目标

- 理解 I/O 表示程序和外部世界交换数据。
- 使用 `Path` 表示文件或目录位置。
- 使用 `Files` 读写 UTF-8 文本文件。
- 使用 `IOException` 表达文件操作失败。
- 使用 JUnit `@TempDir` 测试真实文件读写。
- 在写文件前完成输入校验，避免留下半更新文件。

## 前置知识

已完成[泛型](./07-generics.md)，能够阅读 `List<String>`、只读集合返回、异常边界和 JUnit 断言。

## 什么是 I/O

I/O 是 Input/Output 的缩写，表示输入和输出。程序运行时的数据默认在内存里，进程结束后就会消失。要把学习报告、配置、日志或导入数据保存下来，就需要和文件系统交互。

本章先学习文本文件 I/O：

- 把学习报告写入文件。
- 追加一行新报告。
- 从文件读回多行文本。
- 在测试中使用临时目录，不污染真实工作区。

后续数据库、网络和 Web 都属于更大的 I/O 场景。先把文件 I/O 的边界学稳，会让后面的工程代码更容易理解。

## Path：表示文件位置

现代 Java 推荐使用 `java.nio.file.Path` 表示路径：

```java
Path reportFile = tempDir.resolve("reports").resolve("weekly.txt");
```

`Path` 只表示位置，不保证文件已经存在。上面的代码表示：在临时目录下的 `reports/weekly.txt` 文件。

常见路径操作包括：

- `resolve`：在当前路径后面拼接子路径。
- `getParent`：获取父目录。
- `Files.isRegularFile`：判断路径是否是普通文件。

## Files：读写文本文件

`Files` 是文件操作的工具类。写入多行文本可以使用：

```java
Files.write(reportFile, validatedLines, StandardCharsets.UTF_8);
```

这里显式使用 `StandardCharsets.UTF_8`。不要依赖系统默认编码，否则同一份中文文本在不同机器上可能表现不一致。

读取多行文本可以使用：

```java
List<String> lines = Files.readAllLines(reportFile, StandardCharsets.UTF_8);
```

`readAllLines` 适合小型文本文件。特别大的文件通常需要逐行流式处理，后续学习 Stream 或工程实践时再展开。

## 创建父目录

写文件前，如果父目录不存在，直接写入会失败。可以先创建父目录：

```java
private static void createParentDirectories(Path file) throws IOException {
    Path parent = file.getParent();
    if (parent != null) {
        Files.createDirectories(parent);
    }
}
```

`createDirectories` 会创建缺失的多级目录。如果目录已经存在，它不会报错。

## 追加内容

追加一行可以指定打开选项：

```java
Files.write(reportFile,
        List.of(validatedLine),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
```

`CREATE` 表示文件不存在时创建文件，`APPEND` 表示把内容追加到文件末尾。没有这些选项时，写入通常会覆盖原文件。

## IOException

文件操作可能因为很多外部原因失败：

- 文件不存在。
- 路径其实是目录。
- 父路径没有权限。
- 磁盘空间不足。
- 文件正被其他程序占用。

这些失败不完全由当前方法控制，所以 Java 使用 `IOException` 表达它们。方法签名可以写：

```java
public List<String> load(Path file) throws IOException {
    List<String> lines = Files.readAllLines(requireFile(file), StandardCharsets.UTF_8);
    return Collections.unmodifiableList(lines);
}
```

`throws IOException` 表示调用者必须处理或继续声明这个失败。和前面自定义的 `EnrollmentException` 相比，`IOException` 是受检异常，编译器会要求你面对它。

## 先校验再写文件

文件 I/O 更要注意操作顺序。`StudyReportFileStore.save` 会先校验路径和每一行报告，再创建目录和写文件：

```java
public void save(Path file, List<String> reportLines) throws IOException {
    Path reportFile = requireFile(file);
    List<String> validatedLines = requireReportLines(reportLines);
    createParentDirectories(reportFile);
    Files.write(reportFile, validatedLines, StandardCharsets.UTF_8);
}
```

如果报告列表为空，或者某一行是空白字符串，方法会在写文件前抛出 `IllegalArgumentException`。这样失败时不会留下一个内容不完整的新文件。

## 返回只读结果

读文件得到的是 `List<String>`。为了避免调用者误以为改这个列表就会改文件，示例返回只读列表：

```java
List<String> lines = Files.readAllLines(requireFile(file), StandardCharsets.UTF_8);
return Collections.unmodifiableList(lines);
```

如果要修改文件，应显式调用 `save` 或 `appendLine`。读操作只负责把当前文件内容带回内存。

## 测试真实文件读写

JUnit 5 的 `@TempDir` 可以为每个测试创建临时目录：

```java
@TempDir
Path tempDir;
```

测试可以在这个目录里写真实文件：

```java
Path reportFile = tempDir.resolve("reports").resolve("weekly.txt");
store.save(reportFile, List.of("集合: 完成", "泛型: 复习中"));

assertTrue(store.exists(reportFile));
assertEquals(List.of("集合: 完成", "泛型: 复习中"), store.load(reportFile));
```

测试结束后，JUnit 会清理临时目录。这样既验证了真实文件系统行为，也不会污染项目目录。

## 可运行代码

- [`StudyReportFileStore`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/main/java/io/github/javamastery/core/oop/StudyReportFileStore.java) 展示 `Path`、`Files`、UTF-8 写入、追加、读取和 `IOException`。
- [`StudyReportFileStoreTest`](https://github.com/zhzhlit/java-from-zero-to-mastery/blob/main/examples/java-core/src/test/java/io/github/javamastery/core/oop/StudyReportFileStoreTest.java) 使用 `@TempDir` 覆盖保存、追加、读取、只读结果和异常路径。
- [面向对象练习](https://github.com/zhzhlit/java-from-zero-to-mastery/tree/main/exercises/object-oriented) 提供同主题 starter/solution。

使用 Java 21 运行对应测试：

```bash
mvn -B -pl examples/java-core -am test
mvn -B -pl exercises/object-oriented/starter -am compile
mvn -B -pl exercises/object-oriented/solution -am test
```

## 常见错误与调试提示

- 用字符串手动拼接路径：优先使用 `Path.resolve`，让标准库处理路径分隔符。
- 依赖默认编码：读写中文文本时显式使用 UTF-8。
- 忘记创建父目录：写入嵌套路径前先 `Files.createDirectories`。
- 把 `IOException` 吞掉：调用者会误以为文件操作成功。
- 在项目目录里写测试文件：测试失败时可能留下脏文件。
- 先写文件再校验内容：失败时可能留下半更新文件。

调试文件 I/O 时，先打印或观察 `Path` 的真实位置，再确认文件是否存在、父目录是否创建、异常类型和异常消息是什么。

## 最佳实践及适用边界

- 初学阶段优先使用 `Path` 和 `Files`，少用旧的 `java.io.File`。
- 文本文件读写显式指定 `StandardCharsets.UTF_8`。
- 小文件可以用 `readAllLines`，大文件再考虑逐行处理。
- 测试文件 I/O 时使用 `@TempDir`。
- 方法无法决定如何恢复时，可以继续 `throws IOException`，让调用边界处理。
- 业务输入错误用 `IllegalArgumentException`，文件系统失败用 `IOException`。

## 分级练习

- **基础**：实现 `StudyReportFileStore.save`，把多行学习报告写入 UTF-8 文本文件。
- **进阶**：实现 `load` 和 `exists`，读回报告并判断文件是否存在。
- **挑战**：实现 `appendLine`，并使用 `@TempDir` 测试追加后的完整内容。

## 面试与复习题

1. I/O 表示什么？
2. `Path` 和普通字符串路径相比有什么优势？
3. 为什么读写中文文本要指定 UTF-8？
4. 写文件前为什么要创建父目录？
5. `StandardOpenOption.CREATE` 和 `APPEND` 分别表示什么？
6. `IOException` 为什么是受检异常？
7. `@TempDir` 适合解决什么测试问题？
8. 文件 I/O 中哪些失败属于业务输入错误，哪些属于外部环境错误？

## 本章总结

文件 I/O 让程序可以把内存中的数据保存到磁盘，并在下次运行时读回来。使用 `Path` 表示位置，用 `Files` 读写文本，显式指定 UTF-8，并让 `IOException` 清楚表达外部环境失败。配合 `@TempDir`，文件代码也可以被可靠测试。

## 下一步

上一章：[泛型](./07-generics.md)

继续查看：[学习路线](../../roadmap/index.md)

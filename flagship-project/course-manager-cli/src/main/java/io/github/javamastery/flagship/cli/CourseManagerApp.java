package io.github.javamastery.flagship.cli;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class CourseManagerApp {

    private CourseManagerApp() {
    }

    public static void main(String[] args) throws IOException {
        Path progressFile = args.length == 0
                ? Path.of("target/course-manager-progress.properties")
                : Path.of(args[0]);
        for (String line : run(progressFile, LocalDateTime.now())) {
            System.out.println(line);
        }
    }

    static List<String> run(Path progressFile, LocalDateTime studiedAt) throws IOException {
        CourseCatalog catalog = SampleCourses.coreReviewCatalog();
        Course course = catalog.findByCode("JAVA-CORE")
                .orElseThrow(() -> new IllegalStateException("sample course is missing"));
        ProgressFileStore store = new ProgressFileStore();
        StudyProgress progress = store.exists(progressFile)
                ? store.load(progressFile)
                : new StudyProgress(course.code(), studiedAt, null, java.util.Set.of());

        course.lessons().stream()
                .filter(lesson -> !progress.completed(lesson.id()))
                .findFirst()
                .ifPresent(lesson -> progress.completeLesson(course, lesson.id(), studiedAt));

        store.save(progressFile, progress);

        List<String> lines = new ArrayList<>();
        lines.add("课程数量: " + catalog.courses().size());
        lines.add("总时长: " + catalog.totalDurationMinutes() + " 分钟");
        lines.add("当前进度: " + progress.summary(course));
        lines.add("进度文件: " + progressFile);
        return lines;
    }
}

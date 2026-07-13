package io.github.javamastery.exercises.springbootlayers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseDto> listCourses() {
        return courseRepository.findAll().stream()
                .map(CourseDto::from)
                .toList();
    }

    public Optional<CourseDto> findById(long id) {
        return courseRepository.findById(id).map(CourseDto::from);
    }

    @Transactional
    public CourseDto create(CreateCourseRequest request) {
        String title = normalizeTitle(request.title());
        if (courseRepository.existsByTitleIgnoreCase(title)) {
            throw new BusinessRuleException("课程标题不能重复");
        }
        if (request.lessonCount() <= 0) {
            throw new BusinessRuleException("课时数必须大于 0");
        }
        Course course = new Course(courseRepository.nextId(), title, request.lessonCount(), CourseStatus.DRAFT);
        return CourseDto.from(courseRepository.save(course));
    }

    @Transactional
    public CourseDto publish(long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
        course.publish();
        return CourseDto.from(courseRepository.save(course));
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("课程标题不能为空");
        }
        return title.trim();
    }
}

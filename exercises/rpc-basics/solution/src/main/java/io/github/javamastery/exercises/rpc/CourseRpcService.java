package io.github.javamastery.exercises.rpc;

import io.github.javamastery.exercises.rpc.contract.CourseCatalogGrpc;
import io.github.javamastery.exercises.rpc.contract.CourseReply;
import io.github.javamastery.exercises.rpc.contract.GetCourseRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

public class CourseRpcService extends CourseCatalogGrpc.CourseCatalogImplBase {
    private static final System.Logger LOG = System.getLogger(CourseRpcService.class.getName());
    private final CourseCatalog catalog;

    public CourseRpcService(CourseCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    @Override
    public void getCourse(GetCourseRequest request, StreamObserver<CourseReply> response) {
        long id = request.getCourseId();
        if (id <= 0) {
            response.onError(Status.INVALID_ARGUMENT.withDescription("课程 ID 必须为正数").asRuntimeException());
            return;
        }
        Course course;
        try {
            course = catalog.findById(id).orElse(null);
        } catch (RuntimeException failure) {
            LOG.log(System.Logger.Level.WARNING, "课程查询失败", failure);
            response.onError(Status.INTERNAL.withDescription("课程查询失败").withCause(failure).asRuntimeException());
            return;
        }
        if (course == null) {
            response.onError(Status.NOT_FOUND.withDescription("课程不存在").asRuntimeException());
            return;
        }
        response.onNext(CourseReply.newBuilder().setCourseId(course.id()).setTitle(course.title()).build());
        response.onCompleted();
    }
}

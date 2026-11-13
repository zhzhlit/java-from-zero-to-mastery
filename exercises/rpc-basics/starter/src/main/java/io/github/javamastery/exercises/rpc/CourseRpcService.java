package io.github.javamastery.exercises.rpc;

import io.github.javamastery.exercises.rpc.contract.CourseCatalogGrpc;
import io.github.javamastery.exercises.rpc.contract.CourseReply;
import io.github.javamastery.exercises.rpc.contract.GetCourseRequest;
import io.grpc.stub.StreamObserver;

import java.util.Objects;

public class CourseRpcService extends CourseCatalogGrpc.CourseCatalogImplBase {
    private final CourseCatalog catalog;

    public CourseRpcService(CourseCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
    }

    @Override
    public void getCourse(GetCourseRequest request, StreamObserver<CourseReply> response) {
        throw new UnsupportedOperationException("练习：校验请求、映射状态，成功时只发送一条响应并结束");
    }
}

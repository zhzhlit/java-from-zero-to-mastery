package io.github.javamastery.exercises.rpc;

import io.github.javamastery.exercises.rpc.contract.CourseCatalogGrpc;
import io.github.javamastery.exercises.rpc.contract.GetCourseRequest;
import io.grpc.Channel;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CourseRpcClient {
    private final CourseCatalogGrpc.CourseCatalogBlockingStub stub;
    private final Duration timeout;

    public CourseRpcClient(Channel channel, Duration timeout) {
        Objects.requireNonNull(channel);
        Objects.requireNonNull(timeout);
        if (timeout.isNegative() || timeout.isZero() || timeout.compareTo(Duration.ofSeconds(30)) > 0) {
            throw new IllegalArgumentException("调用预算必须大于零且不超过 30 秒");
        }
        stub = CourseCatalogGrpc.newBlockingStub(channel);
        this.timeout = timeout;
    }

    public Course getCourse(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("课程 ID 必须为正数");
        }
        // Create the deadline for each invocation, not when constructing the reusable client.
        var reply = stub.withDeadlineAfter(timeout.toNanos(), TimeUnit.NANOSECONDS)
                .getCourse(GetCourseRequest.newBuilder().setCourseId(id).build());
        if (reply.getCourseId() != id || reply.getTitle().isBlank() || reply.getTitle().length() > 200) {
            throw new IllegalStateException("RPC 响应课程不合法");
        }
        return new Course(reply.getCourseId(), reply.getTitle());
    }
}

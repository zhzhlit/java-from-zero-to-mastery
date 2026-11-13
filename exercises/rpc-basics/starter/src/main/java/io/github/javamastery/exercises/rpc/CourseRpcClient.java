package io.github.javamastery.exercises.rpc;

import io.github.javamastery.exercises.rpc.contract.CourseCatalogGrpc;
import io.grpc.Channel;

import java.time.Duration;
import java.util.Objects;

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
        throw new UnsupportedOperationException("练习：每次调用设置 deadline，校验响应并保留 RPC 错误状态");
    }
}

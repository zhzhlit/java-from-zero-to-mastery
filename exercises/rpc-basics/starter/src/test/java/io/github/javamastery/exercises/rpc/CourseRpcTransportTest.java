package io.github.javamastery.exercises.rpc;

import io.github.javamastery.exercises.rpc.contract.CourseCatalogGrpc;
import io.github.javamastery.exercises.rpc.contract.CourseReply;
import io.github.javamastery.exercises.rpc.contract.GetCourseRequest;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@org.junit.jupiter.api.Disabled("练习：完成 RPC 服务与客户端后移除 @Disabled")
class CourseRpcTransportTest {
    @Test
    void realHttp2LoopbackCarriesCourseAndErrorStatuses() throws Exception {
        Course course = new Course(1, "Java 远程调用");
        var catalog = new InMemoryCourseCatalog(List.of(course));
        try (var endpoint = new RpcTestEndpoint(new CourseRpcService(catalog), true)) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(3));
            assertEquals(course, client.getCourse(1));
            var missing = assertThrows(StatusRuntimeException.class, () -> client.getCourse(2));
            assertEquals(Status.Code.NOT_FOUND, missing.getStatus().getCode());
            var invalid = assertThrows(StatusRuntimeException.class, () -> CourseCatalogGrpc.newBlockingStub(endpoint.channel)
                    .withDeadlineAfter(3, TimeUnit.SECONDS).getCourse(GetCourseRequest.getDefaultInstance()));
            assertEquals(Status.Code.INVALID_ARGUMENT, invalid.getStatus().getCode());
        }
    }

    @Test
    void serverExceptionDetailsDoNotCrossRealTransport() throws Exception {
        var service = new CourseRpcService(id -> {
            throw new IllegalStateException("private backend detail");
        });
        try (var endpoint = new RpcTestEndpoint(service, true)) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(3));
            var failure = assertThrows(StatusRuntimeException.class, () -> client.getCourse(1));
            assertEquals(Status.Code.INTERNAL, failure.getStatus().getCode());
            assertFalse(failure.getMessage().contains("private backend detail"));
            assertNull(failure.getCause());
        }
    }

    @Test
    void deadlineCancelsRealTransportWithoutArbitraryServerSleep() throws Exception {
        CountDownLatch cancelled = new CountDownLatch(1);
        var service = new CourseCatalogGrpc.CourseCatalogImplBase() {
            @Override
            public void getCourse(GetCourseRequest request, StreamObserver<CourseReply> observer) {
                Context.current().addListener(context -> cancelled.countDown(), Runnable::run);
            }
        };
        try (var endpoint = new RpcTestEndpoint(service, true)) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(1));
            var failure = assertThrows(StatusRuntimeException.class, () -> client.getCourse(1));
            assertEquals(Status.Code.DEADLINE_EXCEEDED, failure.getStatus().getCode());
            assertTrue(cancelled.await(3, TimeUnit.SECONDS));
        }
    }
}

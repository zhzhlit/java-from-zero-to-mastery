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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseRpcTest {
    @Test
    void generatedMessagesRoundTripBinaryAndUnicode() throws Exception {
        var request = GetCourseRequest.newBuilder().setCourseId(42).build();
        var reply = CourseReply.newBuilder().setCourseId(42).setTitle("Java 远程调用").build();
        assertEquals(request, GetCourseRequest.parseFrom(request.toByteArray()));
        assertEquals(reply, CourseReply.parseFrom(reply.toByteArray()));
    }

    @Test
    void catalogCopiesItsInputAndRejectsDuplicateIds() {
        var input = new ArrayList<>(List.of(new Course(1, "Java")));
        var catalog = new InMemoryCourseCatalog(input);
        input.clear();
        assertEquals(Optional.of(new Course(1, "Java")), catalog.findById(1));
        assertTrue(catalog.findById(2).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> new InMemoryCourseCatalog(List.of(new Course(1, "First"), new Course(1, "Second"))));
    }

    @Test
    void courseRejectsInvalidValuesAndNormalizesTitle() {
        assertEquals("Java", new Course(1, " Java ").title());
        assertThrows(IllegalArgumentException.class, () -> new Course(0, "Java"));
        assertThrows(IllegalArgumentException.class, () -> new Course(1, null));
        assertThrows(IllegalArgumentException.class, () -> new Course(1, " "));
        assertThrows(IllegalArgumentException.class, () -> new Course(1, "x".repeat(201)));
    }

    @Test
    void typedClientReturnsCourseAndBorrowsReusableChannel() throws Exception {
        var course = new Course(1, "Java RPC");
        try (var endpoint = endpoint(id -> Optional.of(course))) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(2));
            assertEquals(course, client.getCourse(1));
            assertEquals(course, client.getCourse(1));
            assertFalse(endpoint.channel.isShutdown());
        }
    }

    @Test
    void serverRejectsDefaultAndNegativeIdsBeforeCatalogAccess() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        try (var endpoint = endpoint(id -> {
            calls.incrementAndGet();
            return Optional.empty();
        })) {
            var stub = CourseCatalogGrpc.newBlockingStub(endpoint.channel).withDeadlineAfter(2, TimeUnit.SECONDS);
            for (long id : new long[]{0, -1}) {
                var failure = assertThrows(StatusRuntimeException.class,
                        () -> stub.getCourse(GetCourseRequest.newBuilder().setCourseId(id).build()));
                assertEquals(Status.Code.INVALID_ARGUMENT, failure.getStatus().getCode());
            }
            assertEquals(0, calls.get());
        }
    }

    @Test
    void missingCourseRemainsExplicitNotFound() throws Exception {
        try (var endpoint = endpoint(id -> Optional.empty())) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(2));
            var failure = assertThrows(StatusRuntimeException.class, () -> client.getCourse(99));
            assertEquals(Status.Code.NOT_FOUND, failure.getStatus().getCode());
        }
    }

    @Test
    void repositoryFailureMapsToGenericInternalStatus() throws Exception {
        try (var endpoint = endpoint(id -> {
            throw new IllegalStateException("private backend detail");
        })) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(2));
            var failure = assertThrows(StatusRuntimeException.class, () -> client.getCourse(1));
            assertEquals(Status.Code.INTERNAL, failure.getStatus().getCode());
            assertEquals("课程查询失败", failure.getStatus().getDescription());
        }
    }

    @Test
    void deadlineTerminatesUnansweredCallAndCancelsServerContext() throws Exception {
        CountDownLatch cancelled = new CountDownLatch(1);
        var service = new CourseCatalogGrpc.CourseCatalogImplBase() {
            @Override
            public void getCourse(GetCourseRequest request, StreamObserver<CourseReply> observer) {
                Context.current().addListener(context -> cancelled.countDown(), Runnable::run);
            }
        };
        try (var endpoint = new RpcTestEndpoint(service, false)) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(1));
            var failure = assertThrows(StatusRuntimeException.class, () -> client.getCourse(1));
            assertEquals(Status.Code.DEADLINE_EXCEEDED, failure.getStatus().getCode());
            assertTrue(cancelled.await(2, TimeUnit.SECONDS));
        }
    }

    @Test
    void eachCallGetsNewDeadlineInsteadOfReusingExpiredClientDeadline() throws Exception {
        try (var endpoint = endpoint(id -> Optional.of(new Course(id, "Java")))) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(1));
            assertEquals(1, client.getCourse(1).id());
            Thread.sleep(1100);
            assertEquals(2, client.getCourse(2).id());
        }
    }

    @Test
    void stoppedServerReturnsUnavailableRatherThanEmptyCourse() throws Exception {
        try (var endpoint = endpoint(id -> Optional.of(new Course(id, "Java")))) {
            endpoint.server.shutdownNow();
            assertTrue(endpoint.server.awaitTermination(5, TimeUnit.SECONDS));
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(2));
            var failure = assertThrows(StatusRuntimeException.class, () -> client.getCourse(1));
            assertEquals(Status.Code.UNAVAILABLE, failure.getStatus().getCode());
        }
    }

    @Test
    void invalidClientArgumentsFailLocallyWithoutRpc() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        try (var endpoint = endpoint(id -> {
            calls.incrementAndGet();
            return Optional.empty();
        })) {
            assertThrows(NullPointerException.class, () -> new CourseRpcClient(null, Duration.ofSeconds(1)));
            assertThrows(NullPointerException.class, () -> new CourseRpcClient(endpoint.channel, null));
            for (Duration budget : List.of(Duration.ZERO, Duration.ofSeconds(-1), Duration.ofSeconds(31),
                    Duration.ofSeconds(Long.MAX_VALUE))) {
                assertThrows(IllegalArgumentException.class, () -> new CourseRpcClient(endpoint.channel, budget));
            }
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(1));
            assertThrows(IllegalArgumentException.class, () -> client.getCourse(0));
            assertThrows(IllegalArgumentException.class, () -> client.getCourse(-1));
            assertEquals(0, calls.get());
        }
    }

    @Test
    void malformedRemoteReplyIsRejectedRatherThanTrusted() throws Exception {
        for (CourseReply reply : List.of(CourseReply.newBuilder().setCourseId(2).setTitle("Wrong ID").build(),
                CourseReply.newBuilder().setCourseId(1).setTitle(" ").build(),
                CourseReply.newBuilder().setCourseId(1).setTitle("x".repeat(201)).build())) {
            var service = new CourseCatalogGrpc.CourseCatalogImplBase() {
                @Override
                public void getCourse(GetCourseRequest request, StreamObserver<CourseReply> observer) {
                    observer.onNext(reply);
                    observer.onCompleted();
                }
            };
            try (var endpoint = new RpcTestEndpoint(service, false)) {
                var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(2));
                assertThrows(IllegalStateException.class, () -> client.getCourse(1));
            }
        }
    }

    @Test
    void applicationErrorDoesNotTriggerClientRetryLoop() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        var service = new CourseCatalogGrpc.CourseCatalogImplBase() {
            @Override
            public void getCourse(GetCourseRequest request, StreamObserver<CourseReply> observer) {
                calls.incrementAndGet();
                observer.onError(Status.UNAVAILABLE.withDescription("Unavailable in this test").asRuntimeException());
            }
        };
        try (var endpoint = new RpcTestEndpoint(service, false)) {
            var client = new CourseRpcClient(endpoint.channel, Duration.ofSeconds(2));
            assertThrows(StatusRuntimeException.class, () -> client.getCourse(1));
            assertEquals(1, calls.get());
        }
    }

    private RpcTestEndpoint endpoint(CourseCatalog catalog) throws Exception {
        return new RpcTestEndpoint(new CourseRpcService(catalog), false);
    }
}

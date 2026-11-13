package io.github.javamastery.exercises.rpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RpcBasicsDemo {
    public static void main(String[] args) throws Exception {
        CourseCatalog catalog = new InMemoryCourseCatalog(List.of(new Course(1, "Java RPC 基础")));
        Server server = NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", 0))
                .maxInboundMessageSize(16 * 1024).addService(new CourseRpcService(catalog)).build().start();
        ManagedChannel channel = null;
        try {
            channel = NettyChannelBuilder.forAddress("127.0.0.1", server.getPort()).usePlaintext()
                    .disableRetry().maxInboundMessageSize(16 * 1024).build();
            CourseRpcClient client = new CourseRpcClient(channel, Duration.ofSeconds(2));
            System.out.println("Remote course: " + client.getCourse(1).title());
            try {
                client.getCourse(999);
            } catch (StatusRuntimeException missing) {
                if (missing.getStatus().getCode() != Status.Code.NOT_FOUND) {
                    throw missing;
                }
                System.out.println("Missing course status: " + missing.getStatus().getCode());
            }
        } finally {
            if (channel != null) {
                channel.shutdownNow();
            }
            server.shutdownNow();
            boolean channelStopped = channel == null || channel.awaitTermination(5, TimeUnit.SECONDS);
            boolean serverStopped = server.awaitTermination(5, TimeUnit.SECONDS);
            if (!channelStopped || !serverStopped) {
                throw new IllegalStateException("RPC 资源未按时关闭");
            }
        }
    }
}

package io.github.javamastery.exercises.middleware;

import io.github.javamastery.exercises.rpc.CourseCatalog;
import io.github.javamastery.exercises.rpc.CourseRpcClient;
import io.github.javamastery.exercises.rpc.CourseRpcService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ReviewRpcEndpoint implements AutoCloseable {
    private final Server server;
    private final ManagedChannel channel;

    public ReviewRpcEndpoint(CourseCatalog catalog) throws IOException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", 0))
                .maxInboundMessageSize(16 * 1024).addService(new CourseRpcService(catalog)).build().start();
        try {
            channel = NettyChannelBuilder.forAddress("127.0.0.1", server.getPort()).usePlaintext()
                    .disableRetry().maxInboundMessageSize(16 * 1024).build();
        } catch (RuntimeException | Error failure) {
            server.shutdownNow();
            throw failure;
        }
    }

    public CourseRpcClient client() {
        return new CourseRpcClient(channel, Duration.ofSeconds(3));
    }

    @Override
    public void close() throws InterruptedException {
        channel.shutdownNow();
        server.shutdownNow();
        boolean channelStopped = channel.awaitTermination(5, TimeUnit.SECONDS);
        boolean serverStopped = server.awaitTermination(5, TimeUnit.SECONDS);
        if (!channelStopped || !serverStopped) {
            throw new IllegalStateException("RPC 资源未按时关闭");
        }
    }
}

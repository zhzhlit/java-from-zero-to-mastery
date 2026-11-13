package io.github.javamastery.exercises.rpc;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

class RpcTestEndpoint implements AutoCloseable {
    final Server server;
    final ManagedChannel channel;

    RpcTestEndpoint(BindableService service, boolean tcp) throws IOException {
        String name = InProcessServerBuilder.generateName();
        server = tcp
                ? NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", 0))
                        .maxInboundMessageSize(16 * 1024).addService(service).build().start()
                : InProcessServerBuilder.forName(name).directExecutor().addService(service).build().start();
        try {
            channel = tcp
                    ? NettyChannelBuilder.forAddress("127.0.0.1", server.getPort()).usePlaintext()
                            .disableRetry().maxInboundMessageSize(16 * 1024).build()
                    : InProcessChannelBuilder.forName(name).directExecutor().disableRetry().build();
        } catch (RuntimeException | Error failure) {
            server.shutdownNow();
            throw failure;
        }
    }

    @Override
    public void close() throws InterruptedException {
        channel.shutdownNow();
        server.shutdownNow();
        boolean channelStopped = channel.awaitTermination(5, TimeUnit.SECONDS);
        boolean serverStopped = server.awaitTermination(5, TimeUnit.SECONDS);
        if (!channelStopped || !serverStopped) {
            throw new IllegalStateException("测试 RPC 资源未按时关闭");
        }
    }
}

package com.jstack.jrpc.transport.client;

import com.jstack.jrpc.protocol.Serializer;
import com.jstack.jrpc.transport.codec.RpcDecoder;
import com.jstack.jrpc.transport.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Netty Client Bootstrapper.
 * Responsible for establishing new TCP connections.
 */
public class RpcClientBootstrap {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;

    public RpcClientBootstrap(Serializer serializer) {
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();

        this.bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true) // Disable Nagle's algorithm
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5s timeout
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // 1. HEARTBEAT: Triggers PING if idle for 30s
                        pipeline.addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));

                        // 2. CODEC: Handles serialization/deserialization
                        pipeline.addLast(new RpcEncoder(serializer));
                        pipeline.addLast(new RpcDecoder(serializer));
                        pipeline.addLast(new ClientHandler());

                        // 3. HANDLER: Captures responses and heartbeat events
                        // pipeline.addLast(new ClientHandler());
                    }
                });
    }

    /**
     * Establishes a new TCP connection to the specified address.
     */
    public Channel connect(InetSocketAddress address) {
        try {
            ChannelFuture future = bootstrap.connect(address).sync();
            if (future.isSuccess()) {
                return future.channel();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Connection lost", e);
        }
        return null;
    }

    /**
     * Gracefully shuts down the client resources.
     */
    public void shutdown() {
        group.shutdownGracefully();
    }
}
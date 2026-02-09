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
 * Netty İstemci Başlatıcısı.
 * Yeni bağlantılar oluşturmaktan sorumludur.
 */
public class RpcClientBootstrap {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private final Serializer serializer;

    public RpcClientBootstrap(Serializer serializer) {
        this.serializer = serializer;
        this.group = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();

        this.bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true) // Gecikmeyi azaltır (Nagle Algoritması kapalı)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 saniye bağlanamazsa hata ver
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // 1. HEARTBEAT: 30 saniye boyunca veri yazılmazsa PING atılmasını tetikler
                        pipeline.addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));

                        // 2. CODEC: Baytları nesneye, nesneleri bayta çevirir
                        pipeline.addLast(new RpcEncoder(serializer));
                        pipeline.addLast(new RpcDecoder(serializer));
                        pipeline.addLast(new ClientHandler());

                        // 3. HANDLER: Gelen cevapları ve Heartbeat olaylarını yakalar
                        // (Bunu bir sonraki adımda yazacağız)
                        // pipeline.addLast(new ClientHandler());
                    }
                });
    }

    /**
     * Belirtilen adrese yeni bir TCP bağlantısı açar.
     */
    public Channel connect(InetSocketAddress address) {
        try {
            ChannelFuture future = bootstrap.connect(address).sync();
            if (future.isSuccess()) {
                return future.channel();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bağlantı kesildi", e);
        }
        return null;
    }

    /**
     * İstemciyi kapatır (Kütüphane kapanırken çağrılır).
     */
    public void shutdown() {
        group.shutdownGracefully();
    }
}
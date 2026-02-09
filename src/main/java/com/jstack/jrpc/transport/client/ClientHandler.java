package com.jstack.jrpc.transport.client;

import com.jstack.jrpc.protocol.HeartbeatMessage;
import com.jstack.jrpc.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles client-side events and incoming responses.
 * Clean Code: Focuses on network events and passes data to the next layer.
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // Log as DEBUG to prevent log flooding in production
            log.debug("Client channel {} is idle for 30 seconds, sending PING.", ctx.channel().remoteAddress());
            ctx.writeAndFlush(HeartbeatMessage.PING);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) {
        log.debug("Received response from server. RequestId: {}", response.getRequestId());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Critical errors should always be logged as ERROR
        log.error("Critical error in client channel ({}):", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
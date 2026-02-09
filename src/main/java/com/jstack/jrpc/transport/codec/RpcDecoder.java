package com.jstack.jrpc.transport.codec;

import com.jstack.jrpc.protocol.HeartbeatMessage;
import com.jstack.jrpc.protocol.ProtocolConstants;
import com.jstack.jrpc.protocol.RpcRequest;
import com.jstack.jrpc.protocol.RpcResponse;
import com.jstack.jrpc.protocol.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * Decodes incoming bytes into JRPC protocol objects.
 */
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    private final Serializer serializer;

    public RpcDecoder(Serializer serializer) {
        // Magic(4) + Ver(1) + Type(1) = 6 offset
        super(10 * 1024 * 1024, 6, 4, 0, 0);
        this.serializer = serializer;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (!(decoded instanceof ByteBuf frame)) {
            return null;
        }

        try {
            int magic = frame.readInt();
            if (magic != ProtocolConstants.MAGIC_NUMBER) {
                ctx.close();
                throw new IllegalArgumentException("Unknown magic number: " + magic);
            }

            byte version = frame.readByte();
            if (version != ProtocolConstants.VERSION) {
                ctx.close();
                throw new IllegalArgumentException("Incompatible protocol version: " + version);
            }

            byte type = frame.readByte();
            int bodyLength = frame.readInt();

            // --- HEARTBEAT LOGIC ---
            if (type == ProtocolConstants.MESSAGE_TYPE_HEARTBEAT_PING) {
                // Incoming PING -> Immediate PONG
                ctx.writeAndFlush(HeartbeatMessage.PONG);
                return null;
            } else if (type == ProtocolConstants.MESSAGE_TYPE_HEARTBEAT_PONG) {
                // Incoming PONG -> Silently consume
                return null;
            }
            // -------------------------

            byte[] data = new byte[bodyLength];
            frame.readBytes(data);

            Class<?> targetClass = (type == ProtocolConstants.MESSAGE_TYPE_REQUEST)
                    ? RpcRequest.class
                    : RpcResponse.class;

            return serializer.deserialize(data, targetClass);

        } finally {
            ReferenceCountUtil.release(frame);
        }
    }
}
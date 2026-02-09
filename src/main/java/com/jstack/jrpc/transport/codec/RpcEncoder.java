package com.jstack.jrpc.transport.codec;

import com.jstack.jrpc.protocol.HeartbeatMessage;
import com.jstack.jrpc.protocol.ProtocolConstants;
import com.jstack.jrpc.protocol.RpcRequest;
import com.jstack.jrpc.protocol.RpcResponse;
import com.jstack.jrpc.protocol.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Java nesnelerini JRPC protokol formatında ByteBuf'a dönüştürür.
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {

    private final Serializer serializer;

    public RpcEncoder(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        // 1. Tip Belirleme (Type)
        byte messageType;
        if (msg instanceof RpcRequest) {
            messageType = ProtocolConstants.MESSAGE_TYPE_REQUEST;
        } else if (msg instanceof RpcResponse) {
            messageType = ProtocolConstants.MESSAGE_TYPE_RESPONSE;
        } else if (msg == HeartbeatMessage.PING) {
            messageType = ProtocolConstants.MESSAGE_TYPE_HEARTBEAT_PING;
        } else if (msg == HeartbeatMessage.PONG) {
            messageType = ProtocolConstants.MESSAGE_TYPE_HEARTBEAT_PONG;
        } else {
            return;
        }

        // 2. Serileştirme (Heartbeat paketlerinde payload boştur)
        byte[] data;
        if (messageType == ProtocolConstants.MESSAGE_TYPE_HEARTBEAT_PING ||
                messageType == ProtocolConstants.MESSAGE_TYPE_HEARTBEAT_PONG) {
            data = new byte[0]; // Ping/Pong için veri taşımaya gerek yok
        } else {
            data = serializer.serialize(msg);
        }

        // 3. Yazma İşlemi (Header + Payload)
        out.writeInt(ProtocolConstants.MAGIC_NUMBER); // Magic (4 byte)
        out.writeByte(ProtocolConstants.VERSION); // Version (1 byte)
        out.writeByte(messageType); // Type (1 byte)
        out.writeInt(data.length); // Length (4 byte)
        if (data.length > 0) {
            out.writeBytes(data); // Data (N byte)
        }
    }
}
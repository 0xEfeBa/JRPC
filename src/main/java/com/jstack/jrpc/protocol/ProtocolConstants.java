package com.jstack.jrpc.protocol;

public final class ProtocolConstants {

    private ProtocolConstants() {
    }

    /**
     * Magic Number: 0xCAFEBABE (4 byte)
     */
    public static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * Protocol Version: 1 (1 byte)
     */
    public static final byte VERSION = 0x01;

    /**
     * Message Type: Request (1 byte)
     */
    public static final byte MESSAGE_TYPE_REQUEST = 0x01;

    /**
     * Message Type: Response (1 byte)
     */
    public static final byte MESSAGE_TYPE_RESPONSE = 0x02;

    /**
     * Message Type: Heartbeat PING (1 byte) - Client -> Server
     */
    public static final byte MESSAGE_TYPE_HEARTBEAT_PING = 0x03;

    /**
     * Message Type: Heartbeat PONG (1 byte) - Server -> Client
     */
    public static final byte MESSAGE_TYPE_HEARTBEAT_PONG = 0x04;

    /**
     * Total Header Length: 10 Bytes
     * Calculation: Magic(4) + Version(1) + Type(1) + FullLength(4) = 10
     */
    public static final int HEADER_TOTAL_LENGTH = 10;
}
package com.jstack.jrpc.protocol;

import java.io.Serializable;

/**
 * Immutable message types used for network liveness validation (Heartbeat).
 * Using static final instances (PING/PONG) to minimize object creation
 * overhead.
 */
public class HeartbeatMessage implements Serializable {

    public static final HeartbeatMessage PING = new HeartbeatMessage();
    public static final HeartbeatMessage PONG = new HeartbeatMessage();

    private HeartbeatMessage() {
        // Enforce singleton-like usage for predefined instances
    }

}
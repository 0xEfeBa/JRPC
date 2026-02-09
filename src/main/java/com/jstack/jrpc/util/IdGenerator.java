package com.jstack.jrpc.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe unique ID generator for RPC requests.
 * Uses {@link AtomicLong} to ensure consistency across concurrent network
 * threads.
 */
public class IdGenerator {

    private static final AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    private IdGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates and returns the next unique requestId.
     * 
     * @return A monotonically increasing unique ID.
     */
    public static long nextId() {
        return REQUEST_ID_GEN.incrementAndGet();
    }

}

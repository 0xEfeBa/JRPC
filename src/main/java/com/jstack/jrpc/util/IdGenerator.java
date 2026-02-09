package com.jstack.jrpc.util;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static final AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    private IdGenerator() {

    }

    public static long nextId() {
        return REQUEST_ID_GEN.incrementAndGet();
    }

}

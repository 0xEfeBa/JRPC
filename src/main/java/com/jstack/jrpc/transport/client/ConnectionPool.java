package com.jstack.jrpc.transport.client;

import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Connection Pool.
 * Manages TCP connections and ensures reusability with host-based limits.
 * Professional implementation using Semaphores for concurrency control.
 */
@Slf4j
public class ConnectionPool {

    // Channels waiting in idle state
    private final Map<String, BlockingQueue<Channel>> idleChannels = new ConcurrentHashMap<>();

    // Concurrency control per host using Semaphores
    private final Map<String, Semaphore> hostSemaphores = new ConcurrentHashMap<>();

    private final RpcClientBootstrap bootstrap;
    private final int maxConnectionsPerHost;
    private final long acquireTimeout;

    public ConnectionPool(RpcClientBootstrap bootstrap, int maxConnectionsPerHost, long acquireTimeout) {
        this.bootstrap = bootstrap;
        this.maxConnectionsPerHost = maxConnectionsPerHost;
        this.acquireTimeout = acquireTimeout;
    }

    /**
     * Acquires a channel from the pool or creates a new one if limits allow.
     * 1. Check idle queue first.
     * 2. If no idle channel exists, try to get a permit from Semaphore to create a
     * new one.
     */
    public Channel acquire(InetSocketAddress address) {
        String key = address.toString();
        BlockingQueue<Channel> queue = idleChannels.computeIfAbsent(key,
                k -> new LinkedBlockingQueue<>(maxConnectionsPerHost));

        // 1. Check for available idle channel
        Channel channel = queue.poll();
        if (channel != null && channel.isActive()) {
            return channel;
        }

        // 2. Otherwise, request permit to create a new connection
        Semaphore semaphore = hostSemaphores.computeIfAbsent(key,
                k -> new Semaphore(maxConnectionsPerHost));

        try {
            // Wait for permit within timeout
            if (semaphore.tryAcquire(acquireTimeout, TimeUnit.MILLISECONDS)) {
                try {
                    Channel newChannel = bootstrap.connect(address);
                    if (newChannel != null) {
                        return newChannel;
                    }
                } catch (Exception e) {
                    semaphore.release(); // Release permit if connection fails
                    throw e;
                }
                semaphore.release(); // Release if connect returned null
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Acquire interrupted for: " + key, e);
        }

        throw new RuntimeException("Connection pool exhausted for host: " + key);
    }

    /**
     * Releases a channel back to the pool or closes it if no longer needed/active.
     */
    public void release(InetSocketAddress address, Channel channel) {
        String key = address.toString();

        // If channel is invalid, release permit instead of pooling
        if (channel == null || !channel.isActive()) {
            releasePermit(key);
            return;
        }

        BlockingQueue<Channel> queue = idleChannels.get(key);

        // Try returning channel to idle queue
        if (queue != null && queue.offer(channel)) {
            log.debug("Channel returned to pool: {}", key);
        } else {
            // Close channel if pool is full
            channel.close();
            releasePermit(key);
        }
    }

    /**
     * Increments the connection permit for the specified host.
     */
    private void releasePermit(String key) {
        Semaphore semaphore = hostSemaphores.get(key);
        if (semaphore != null) {
            semaphore.release();
        }
    }
}
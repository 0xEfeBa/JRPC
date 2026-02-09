package com.jstack.jrpc.transport.client;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConnectionPoolTest {

    private ConnectionPool pool;

    @Mock
    private RpcClientBootstrap bootstrap;

    @Mock
    private Channel mockChannel;

    private final InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);

    @BeforeEach
    void setUp() {
        // Limit 2, Timeout 100ms
        pool = new ConnectionPool(bootstrap, 2, 100);
    }

    @Test
    void testAcquireNewConnection() {
        when(bootstrap.connect(any())).thenReturn(mockChannel);

        Channel channel = pool.acquire(address);

        Assertions.assertNotNull(channel);
        verify(bootstrap, times(1)).connect(address);
    }

    @Test
    void testConnectionReuse() {
        when(bootstrap.connect(any())).thenReturn(mockChannel);
        // isActive() only called for channels taken from idle pool
        when(mockChannel.isActive()).thenReturn(true);

        // 1. Acquire and release
        Channel ch1 = pool.acquire(address);
        pool.release(address, ch1);

        // 2. Acquire again (should come from pool)
        Channel ch2 = pool.acquire(address);

        Assertions.assertSame(ch1, ch2);
        verify(bootstrap, times(1)).connect(any());
    }

    @Test
    void testPoolExhausted() {
        when(bootstrap.connect(any())).thenReturn(mockChannel);

        // Fill pool (Limit 2)
        pool.acquire(address);
        pool.acquire(address);

        // 3rd acquire should fail
        Assertions.assertThrows(RuntimeException.class, () -> {
            pool.acquire(address);
        });
    }
}

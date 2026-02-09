package com.jstack.jrpc.protocol;

import com.jstack.jrpc.api.JRpcConfig;
import com.jstack.jrpc.util.IdGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class SerializationTest {

    @Test
    public void testRpcRequestSerialization() {
        // 1. Setup: Create config and Serializer
        JRpcConfig config = new JRpcConfig.Builder().useCompatibleFieldSerializer(false).build();
        KryoSerializer serializer = new KryoSerializer(config);

        // 2. Scenario: Create an RpcRequest (with new tracing fields)
        RpcRequest request = RpcRequest.builder()
                .traceId(UUID.randomUUID().toString())
                .requestId(IdGenerator.nextId())
                .serviceName("userService")
                .methodName("getUser")
                .paramTypes(new Class[] { Long.class })
                .parameters(new Object[] { 123L })
                .build();

        // 3. Action: Serialize and Deserialize
        byte[] bytes = serializer.serialize(request);
        RpcRequest decodedRequest = serializer.deserialize(bytes, RpcRequest.class);

        // 4. Verification: Check if data is preserved
        Assertions.assertEquals(request.getTraceId(), decodedRequest.getTraceId());
        Assertions.assertEquals(request.getRequestId(), decodedRequest.getRequestId());
        Assertions.assertEquals(request.getMethodName(), decodedRequest.getMethodName());
        Assertions.assertArrayEquals(request.getParameters(), decodedRequest.getParameters());

        System.out.println("Serialization Test Passed: " + decodedRequest);
    }

    @Test
    public void testIdGeneratorUniqueness() {
        // Simple loop to check if IDs are strictly increasing
        long id1 = IdGenerator.nextId();
        long id2 = IdGenerator.nextId();

        Assertions.assertTrue(id2 > id1, "IDs must be in increasing order");
    }
}
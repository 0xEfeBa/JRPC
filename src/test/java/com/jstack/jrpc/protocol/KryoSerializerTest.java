package com.jstack.jrpc.protocol;

import com.jstack.jrpc.api.JRpcConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KryoSerializerTest {

    @Test
    public void testPerformanceModeSerialization() {
        // 1. Config: Performance Mode (Compatible DISABLED)
        JRpcConfig config = new JRpcConfig.Builder()
                .useCompatibleFieldSerializer(false)
                .build();

        KryoSerializer serializer = new KryoSerializer(config);

        // 2. Prepare Data
        RpcRequest request = new RpcRequest();
        request.setTraceId("trace-1001");
        request.setRequestId(555L);
        request.setServiceName("PaymentService");
        request.setMethodName("processPayment");
        request.setParamTypes(new Class[] { int.class, String.class });
        request.setParameters(new Object[] { 100, "TR_CC_NO" });

        // 3. Execute Serialization
        long startTime = System.nanoTime();
        byte[] bytes = serializer.serialize(request);
        long endTime = System.nanoTime();

        // 4. Log results and payload size
        System.out.println("=== Performance Mode ===");
        System.out.println("Serialization Time: " + (endTime - startTime) + " ns");
        System.out.println("Payload Size: " + bytes.length + " bytes");

        // 5. Execute Deserialization and Verify
        RpcRequest decoded = serializer.deserialize(bytes, RpcRequest.class);

        Assertions.assertEquals(request.getTraceId(), decoded.getTraceId());
        Assertions.assertEquals(request.getRequestId(), decoded.getRequestId());
        Assertions.assertEquals(request.getMethodName(), decoded.getMethodName());
        Assertions.assertArrayEquals(request.getParameters(), decoded.getParameters());
    }
}
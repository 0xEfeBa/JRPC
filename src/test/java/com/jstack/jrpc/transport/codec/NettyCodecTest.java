package com.jstack.jrpc.transport.codec;

import com.jstack.jrpc.api.JRpcConfig;
import com.jstack.jrpc.protocol.KryoSerializer;
import com.jstack.jrpc.protocol.RpcRequest;
import com.jstack.jrpc.util.IdGenerator;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class NettyCodecTest {

    @Test
    public void testEncodeAndDecode() {
        // 1. Hazırlık: Serializer ve Konfigürasyon
        JRpcConfig config = new JRpcConfig.Builder().useCompatibleFieldSerializer(false).build();
        KryoSerializer serializer = new KryoSerializer(config);

        // 2. EmbeddedChannel Kurulumu: Pipeline'a yazdığımız Encoder ve Decoder'ı
        // ekliyoruz.
        EmbeddedChannel channel = new EmbeddedChannel(
                new RpcDecoder(serializer),
                new RpcEncoder(serializer));

        // 3. Senaryo: Bir RpcRequest Nesnesi Oluştur
        RpcRequest request = RpcRequest.builder()
                .requestId(IdGenerator.nextId())
                .traceId(UUID.randomUUID().toString())
                .serviceName("TestService")
                .methodName("hello")
                .parameters(new Object[] { "World" })
                .paramTypes(new Class[] { String.class })
                .build();

        // 4. Eylem: Nesneyi kanala "yaz" (Outbound - Encoding başlar)
        channel.writeOutbound(request);

        // 5. Ara Adım: Encoder'dan çıkan baytları al ve Decoder'ın girişine ver
        // (Inbound - Decoding başlar)
        Object encodedData = channel.readOutbound();
        channel.writeInbound(encodedData);

        // 6. Sonuç: Decoder'dan çıkan nesneyi oku ve doğrula
        RpcRequest decodedRequest = channel.readInbound();

        Assertions.assertNotNull(decodedRequest);
        Assertions.assertEquals(request.getRequestId(), decodedRequest.getRequestId());
        Assertions.assertEquals(request.getTraceId(), decodedRequest.getTraceId());
        Assertions.assertEquals(request.getServiceName(), decodedRequest.getServiceName());
        Assertions.assertEquals(request.getMethodName(), decodedRequest.getMethodName());
        Assertions.assertArrayEquals(request.getParameters(), decodedRequest.getParameters());

        System.out.println("Netty Codec Test Passed! Decoded Object: " + decodedRequest);
    }
}
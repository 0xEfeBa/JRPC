package com.jstack.jrpc.protocol;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.jstack.jrpc.api.JRpcConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * High-performance Kryo implementation of {@link Serializer}.
 * Thread-safety is achieved via ThreadLocal to eliminate synchronization
 * overhead.
 */
public class KryoSerializer implements Serializer {

    private final ThreadLocal<Kryo> kryoThreadLocal;

    public KryoSerializer(JRpcConfig config) {
        this.kryoThreadLocal = ThreadLocal.withInitial(() -> {
            Kryo kryo = new Kryo();

            // Set serialization strategy based on performance vs evolution needs
            if (config.isUseCompatibleFieldSerializer()) {
                kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            } else {
                kryo.setDefaultSerializer(FieldSerializer.class);
            }

            // Registering classes for optimized payload (ID mapping instead of full class
            // names)
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            kryo.register(Class.class);
            kryo.register(Object[].class);
            kryo.register(Class[].class);

            kryo.setRegistrationRequired(false);
            return kryo;
        });
    }

    /**
     * Serializes an object to a byte array using internal buffer management.
     */
    @Override
    public byte[] serialize(Object obj) {
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        kryo.writeObject(output, obj);
        output.close();

        return bos.toByteArray();
    }

    /**
     * Deserializes a byte array back into an instance of clazz.
     * Note: Clazz parameter prevents dynamic type resolution overhead.
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = kryoThreadLocal.get();
        Input input = new Input(new ByteArrayInputStream(bytes));
        return kryo.readObject(input, clazz);
    }
}
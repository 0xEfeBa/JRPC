package com.jstack.jrpc.protocol;

/**
 * Base interface for JRPC serialization strategies (e.g., Kryo).
 * Implementations must provide thread-safe methods to convert objects to bytes
 * and vice versa.
 */
public interface Serializer {
    /**
     * Serializes an object to a byte array.
     * 
     * @param obj The object to serialize.
     * @return Serialized byte array.
     */
    byte[] serialize(Object obj);

    /**
     * Deserializes a byte array back to an instance of the specified class.
     * 
     * @param bytes Serialized data.
     * @param clazz Target class for reconstruction.
     * @param <T>   Type of the returned object.
     * @return Reconstructed object instance.
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
package com.jstack.jrpc.api;

/**
 * Central configuration for the JRPC framework.
 * Use {@link Builder} for instantiation.
 */
public class JRpcConfig {

    /**
     * If enabled, uses a serializer that supports backward compatibility (slower)
     */
    private boolean useCompatibleFieldSerializer = false;

    private JRpcConfig() {
    }

    public boolean isUseCompatibleFieldSerializer() {
        return useCompatibleFieldSerializer;
    }

    /**
     * Builder for granular JRpcConfig setup.
     */
    public static class Builder {
        private final JRpcConfig config = new JRpcConfig();

        /**
         * @param enable true for 'Compatible Mode' (flexible schema),
         *               false for 'Performance Mode' (standard serialization).
         */
        public Builder useCompatibleFieldSerializer(boolean enable) {
            config.useCompatibleFieldSerializer = enable;
            return this;
        }

        public JRpcConfig build() {
            return config;
        }
    }
}
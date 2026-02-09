package com.jstack.jrpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object representing an RPC request.
 * Contains all necessary metadata for remote method invocation and distributed
 * tracing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

    private String traceId; // Technical ID for distributed tracing
    private long requestId; // Correlation ID for Request/Response pairing
    private String serviceName;
    private String methodName;
    private Class<?>[] paramTypes; // Reflection-based parameter types
    private Object[] parameters; // Execution arguments
}

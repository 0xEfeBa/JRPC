package com.jstack.jrpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {

    private String traceId; // Inherited from original request for tracing
    private long requestId; // Matches the request's correlation ID
    private Object result; // Successfully returned object
    private Throwable error; // Exception thrown during execution
    private boolean success; // Status indicator

}
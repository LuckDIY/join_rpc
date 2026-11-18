package io.github.chaoleiwang.rpc.exception;

/**
 * RPC 调用异常
 */
public class RpcException extends RuntimeException {

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

}

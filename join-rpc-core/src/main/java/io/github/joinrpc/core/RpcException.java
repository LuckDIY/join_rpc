package io.github.joinrpc.core;

/**
 * join-rpc 框架异常基类。
 */
public class RpcException extends RuntimeException {

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
}

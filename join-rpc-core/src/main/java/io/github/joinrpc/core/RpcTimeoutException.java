package io.github.joinrpc.core;

/**
 * 调用在超时时间内没有收到响应。请求可能已经在服务端执行。
 */
public class RpcTimeoutException extends RpcException {

    public RpcTimeoutException(String message) {
        super(message);
    }
}

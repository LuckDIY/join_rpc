package io.github.joinrpc.core.client;

import io.github.joinrpc.core.RpcException;

/**
 * 连接服务端失败。此时请求一定还没发出去，可以安全地换一个节点重试。
 */
public class RpcConnectException extends RpcException {

    public RpcConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}

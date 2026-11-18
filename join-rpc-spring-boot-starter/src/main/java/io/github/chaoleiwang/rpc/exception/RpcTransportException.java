package io.github.chaoleiwang.rpc.exception;

/**
 * RPC 传输层异常(连接失败、超时、连接断开等)。
 * <p>
 * 与业务异常区分:传输层异常发生时客户端会尝试下一个可用实例,业务异常直接抛出。
 */
public class RpcTransportException extends RpcException {

    public RpcTransportException(String message) {
        super(message);
    }

    public RpcTransportException(String message, Throwable cause) {
        super(message, cause);
    }

}

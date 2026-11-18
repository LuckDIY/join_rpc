package io.github.joinrpc.core;

/**
 * 服务端业务方法抛出的异常。异常对象本身不跨网络传输，只带回异常类名和消息。
 */
public class RpcRemoteException extends RpcException {

    private final String remoteExceptionClass;

    public RpcRemoteException(String remoteExceptionClass, String message) {
        super(remoteExceptionClass + ": " + message);
        this.remoteExceptionClass = remoteExceptionClass;
    }

    /**
     * @return 服务端抛出的异常的全限定类名
     */
    public String getRemoteExceptionClass() {
        return remoteExceptionClass;
    }
}

package io.github.joinrpc.spring.cloud;

/**
 * 写在注册中心实例 metadata 里的 key。
 */
public final class RpcMetadata {

    /** 该实例的 RPC 端口。没有这个 key 的实例不会被当作 join-rpc 提供方 */
    public static final String PORT = "join-rpc.port";

    private RpcMetadata() {
    }
}

package io.github.joinrpc.core;

import java.util.Objects;

/**
 * RPC 服务端地址。
 */
public record RpcAddress(String host, int port) {

    public RpcAddress {
        Objects.requireNonNull(host, "host");
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("invalid port: " + port);
        }
    }

    /**
     * 解析 "host:port" 格式的地址。
     */
    public static RpcAddress parse(String address) {
        String value = address.trim();
        int idx = value.lastIndexOf(':');
        if (idx <= 0 || idx == value.length() - 1) {
            throw new IllegalArgumentException("address must be host:port, but was '" + address + "'");
        }
        return new RpcAddress(value.substring(0, idx), Integer.parseInt(value.substring(idx + 1)));
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}

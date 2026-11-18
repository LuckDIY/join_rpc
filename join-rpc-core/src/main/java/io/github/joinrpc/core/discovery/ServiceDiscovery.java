package io.github.joinrpc.core.discovery;

import io.github.joinrpc.core.RpcAddress;

import java.util.List;

/**
 * 根据提供方应用名查找 RPC 地址。
 */
public interface ServiceDiscovery {

    /**
     * @param application 提供方应用名（注册中心里的 serviceId）
     * @return 可用地址，没有时返回空列表，不返回 null
     */
    List<RpcAddress> getAddresses(String application);

    /**
     * 通知缓存失效，例如连接某个地址失败时调用。
     */
    default void invalidate(String application) {
    }
}

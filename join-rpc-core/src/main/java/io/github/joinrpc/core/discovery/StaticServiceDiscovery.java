package io.github.joinrpc.core.discovery;

import io.github.joinrpc.core.RpcAddress;

import java.util.List;
import java.util.Map;

/**
 * 固定地址列表，用于直连、本地调试和测试。
 */
public class StaticServiceDiscovery implements ServiceDiscovery {

    private final Map<String, List<RpcAddress>> addresses;

    public StaticServiceDiscovery(Map<String, List<RpcAddress>> addresses) {
        this.addresses = Map.copyOf(addresses);
    }

    @Override
    public List<RpcAddress> getAddresses(String application) {
        return addresses.getOrDefault(application, List.of());
    }
}

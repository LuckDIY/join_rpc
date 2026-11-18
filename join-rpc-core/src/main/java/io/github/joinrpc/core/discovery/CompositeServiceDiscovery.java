package io.github.joinrpc.core.discovery;

import io.github.joinrpc.core.RpcAddress;

import java.util.List;

/**
 * 按顺序查询，返回第一个非空结果。例如：先查直连配置，没有再查注册中心。
 */
public class CompositeServiceDiscovery implements ServiceDiscovery {

    private final List<ServiceDiscovery> delegates;

    public CompositeServiceDiscovery(List<ServiceDiscovery> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public List<RpcAddress> getAddresses(String application) {
        for (ServiceDiscovery delegate : delegates) {
            List<RpcAddress> addresses = delegate.getAddresses(application);
            if (!addresses.isEmpty()) {
                return addresses;
            }
        }
        return List.of();
    }

    @Override
    public void invalidate(String application) {
        delegates.forEach(d -> d.invalidate(application));
    }
}

package io.github.chaoleiwang.rpc.client.discovery;

import io.github.chaoleiwang.rpc.common.model.RpcServiceInstance;
import io.github.chaoleiwang.rpc.exception.RpcException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 直连模式服务发现:不经过注册中心,直接使用配置的 host:port 列表。
 * <p>
 * 配置示例: rpc.client.direct-address=127.0.0.1:9999,192.168.1.10:9999
 */
public class DirectAddressServiceDiscovery implements RpcServiceDiscovery {

    private final List<RpcServiceInstance> instances;

    public DirectAddressServiceDiscovery(String directAddress) {
        if (directAddress == null || directAddress.isBlank()) {
            throw new IllegalArgumentException("directAddress 不能为空");
        }
        List<RpcServiceInstance> list = new ArrayList<>();
        for (String address : directAddress.split(",")) {
            String trimmed = address.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("直连地址格式错误(应为 host:port): " + trimmed);
            }
            try {
                list.add(new RpcServiceInstance(parts[0], Integer.parseInt(parts[1].trim()), Collections.emptyMap()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("直连地址端口格式错误: " + trimmed, e);
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("直连地址列表为空");
        }
        this.instances = list;
    }

    @Override
    public List<RpcServiceInstance> getInstances(String serviceName) {
        // 直连模式下不区分服务名,所有服务都指向配置的地址
        return instances;
    }

}

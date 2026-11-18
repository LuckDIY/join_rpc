package io.github.chaoleiwang.rpc.client.discovery;

import io.github.chaoleiwang.rpc.common.model.RpcServiceInstance;
import io.github.chaoleiwang.rpc.exception.RpcException;

import java.util.List;

/**
 * 无注册中心、无直连地址时的服务发现:调用时给出明确报错
 */
public class NoRegistryServiceDiscovery implements RpcServiceDiscovery {

    @Override
    public List<RpcServiceInstance> getInstances(String serviceName) {
        throw new RpcException("未接入注册中心且未配置 rpc.client.direct-address,"
                + "无法发现服务[" + serviceName + "]。请引入注册中心 starter(如 spring-cloud-starter-alibaba-nacos-discovery)"
                + "或配置直连地址");
    }

}

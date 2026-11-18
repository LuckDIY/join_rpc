package io.github.chaoleiwang.rpc.client.discovery;

import io.github.chaoleiwang.rpc.common.model.RpcServiceInstance;

import java.util.List;

/**
 * 服务发现接口:按服务名获取可用实例列表,不绑定具体注册中心。
 * <p>
 * 默认实现基于 Spring Cloud 的 DiscoveryClient / LoadBalancerClient 抽象;
 * 也支持 rpc.client.direct-address 直连模式,无需注册中心。
 */
public interface RpcServiceDiscovery {

    /**
     * 获取服务的可用实例列表
     *
     * @param serviceName 服务名
     * @return 可用实例,不存在时返回空列表或抛出异常
     */
    List<RpcServiceInstance> getInstances(String serviceName);

}

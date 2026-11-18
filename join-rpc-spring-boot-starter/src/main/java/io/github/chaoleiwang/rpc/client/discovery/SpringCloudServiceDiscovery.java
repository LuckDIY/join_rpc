package io.github.chaoleiwang.rpc.client.discovery;

import io.github.chaoleiwang.rpc.common.model.RpcServiceInstance;
import io.github.chaoleiwang.rpc.config.RpcProperties;
import io.github.chaoleiwang.rpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 Spring Cloud 抽象的服务发现:
 * <ul>
 *   <li>配置了 rpc.client.direct-address 时优先直连;</li>
 *   <li>存在 LoadBalancerClient Bean 时走负载均衡(spring-cloud-loadbalancer);</li>
 *   <li>否则走 DiscoveryClient + 内置 simple 负载均衡。</li>
 * </ul>
 */
@Slf4j
public class SpringCloudServiceDiscovery implements RpcServiceDiscovery {

    private final RpcProperties properties;
    private final ObjectProvider<DiscoveryClient> discoveryClientProvider;
    private final ObjectProvider<LoadBalancerClient> loadBalancerClientProvider;

    private final DirectAddressServiceDiscovery directAddressDiscovery;
    private final SimpleLoadBalancer simpleLoadBalancer;

    public SpringCloudServiceDiscovery(RpcProperties properties,
                                       ObjectProvider<DiscoveryClient> discoveryClientProvider,
                                       ObjectProvider<LoadBalancerClient> loadBalancerClientProvider) {
        this.properties = properties;
        this.discoveryClientProvider = discoveryClientProvider;
        this.loadBalancerClientProvider = loadBalancerClientProvider;
        this.directAddressDiscovery = StringUtils.hasText(properties.getClient().getDirectAddress())
                ? new DirectAddressServiceDiscovery(properties.getClient().getDirectAddress())
                : null;
        this.simpleLoadBalancer = new SimpleLoadBalancer(properties.getClient().getLoadBalance());
    }

    @Override
    public List<RpcServiceInstance> getInstances(String serviceName) {
        // 1. 直连地址优先
        if (directAddressDiscovery != null) {
            return directAddressDiscovery.getInstances(serviceName);
        }
        // 2. Spring Cloud LoadBalancer(如果使用方引入了 spring-cloud-loadbalancer)
        LoadBalancerClient loadBalancerClient = loadBalancerClientProvider.getIfAvailable();
        if (loadBalancerClient != null) {
            try {
                ServiceInstance instance = loadBalancerClient.choose(serviceName);
                if (instance != null) {
                    return Collections.singletonList(wrap(instance));
                }
            } catch (Exception e) {
                log.debug("LoadBalancerClient 选择实例失败,回退到 DiscoveryClient: {}", e.getMessage());
            }
        }
        // 3. DiscoveryClient + 内置负载均衡
        DiscoveryClient discoveryClient = discoveryClientProvider.getIfAvailable();
        if (discoveryClient != null) {
            List<RpcServiceInstance> instances = discoveryClient.getInstances(serviceName).stream()
                    .map(this::wrap)
                    .collect(Collectors.toList());
            if (!instances.isEmpty()) {
                return simpleLoadBalancer.order(instances, serviceName);
            }
        }
        throw new RpcException("未找到服务[" + serviceName + "]的可用实例,请检查服务端是否启动并已注册到注册中心");
    }

    private RpcServiceInstance wrap(ServiceInstance instance) {
        return new RpcServiceInstance(instance.getHost(), instance.getPort(), instance.getMetadata());
    }

}

package io.github.joinrpc.spring.cloud;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.discovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 通过 Spring Cloud 标准的 {@link DiscoveryClient} 查找提供方，适用于任何实现了它的注册中心
 * （Nacos、Consul、Eureka、Zookeeper、Kubernetes...）。
 * <p>
 * 地址 = 实例的 host + metadata 中的 {@link RpcMetadata#PORT}。没有该 metadata 的实例（比如普通 HTTP 服务）会被忽略。
 */
public class SpringCloudServiceDiscovery implements ServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(SpringCloudServiceDiscovery.class);

    private final ObjectProvider<DiscoveryClient> discoveryClient;

    public SpringCloudServiceDiscovery(ObjectProvider<DiscoveryClient> discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public List<RpcAddress> getAddresses(String application) {
        // 有多个 DiscoveryClient 时，Spring Cloud 会提供一个 @Primary 的 CompositeDiscoveryClient
        DiscoveryClient client = discoveryClient.getIfAvailable();
        if (client == null) {
            return List.of();
        }
        List<ServiceInstance> instances = client.getInstances(application);
        if (instances == null) {
            return List.of();
        }
        List<RpcAddress> addresses = instances.stream().map(this::toAddress).filter(Objects::nonNull).toList();
        if (addresses.isEmpty() && !instances.isEmpty()) {
            log.warn("application '{}' has {} instances but none has metadata '{}', is join-rpc running there?",
                    application, instances.size(), RpcMetadata.PORT);
        }
        return addresses;
    }

    private RpcAddress toAddress(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String port = metadata == null ? null : metadata.get(RpcMetadata.PORT);
        if (port == null || port.isBlank()) {
            return null;
        }
        try {
            return new RpcAddress(instance.getHost(), Integer.parseInt(port.trim()));
        } catch (IllegalArgumentException e) {
            log.warn("ignore instance {}:{} of '{}', invalid metadata {}={}", instance.getHost(), instance.getPort(),
                    instance.getServiceId(), RpcMetadata.PORT, port);
            return null;
        }
    }
}

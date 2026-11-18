package io.github.chaoleiwang.rpc.config;

import io.github.chaoleiwang.rpc.client.RpcClient;
import io.github.chaoleiwang.rpc.client.discovery.DirectAddressServiceDiscovery;
import io.github.chaoleiwang.rpc.client.discovery.NoRegistryServiceDiscovery;
import io.github.chaoleiwang.rpc.client.discovery.RpcServiceDiscovery;
import io.github.chaoleiwang.rpc.client.discovery.SpringCloudServiceDiscovery;
import io.github.chaoleiwang.rpc.common.serialize.JsonMessageSerialize;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.github.chaoleiwang.rpc.common.serialize.ProtostuffMessageSerialize;
import io.github.chaoleiwang.rpc.server.NettyRpcServer;
import io.github.chaoleiwang.rpc.server.exporter.RpcServiceExporter;
import io.github.chaoleiwang.rpc.server.handler.RpcRequestHandler;
import io.github.chaoleiwang.rpc.server.registry.LocalServiceRepository;
import io.github.chaoleiwang.rpc.server.registry.NoopRpcServiceRegistry;
import io.github.chaoleiwang.rpc.server.registry.RpcServiceRegistry;
import io.github.chaoleiwang.rpc.server.registry.ServiceRepository;
import io.github.chaoleiwang.rpc.server.registry.SpringCloudRpcServiceRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * join-rpc 自动装配。
 * <p>
 * 注册中心不绑定具体实现:classpath 上存在 Spring Cloud 时,
 * 优先使用 {@code ServiceRegistry} / {@code DiscoveryClient} 抽象(Nacos、Eureka 等由使用方引入),
 * 没有注册中心时自动退化为直连/本地模式。
 * <p>
 * 注意:本类中带 Spring Cloud 类型的 Bean 方法放在前面声明,
 * 保证 @ConditionalOnMissingBean 按声明顺序正确评估。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "rpc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RpcProperties.class)
public class RpcAutoConfiguration {

    // ==================== 序列化 ====================

    @Bean
    @ConditionalOnMissingBean
    public MessageSerialize messageSerialize(RpcProperties properties) {
        if ("protostuff".equalsIgnoreCase(properties.getSerializer())) {
            return new ProtostuffMessageSerialize();
        }
        if ("json".equalsIgnoreCase(properties.getSerializer())) {
            return new JsonMessageSerialize();
        }
        throw new IllegalStateException("不支持的序列化协议: " + properties.getSerializer() + ", 可选: json / protostuff");
    }

    // ==================== 服务端 ====================

    @Bean
    @ConditionalOnMissingBean
    public ServiceRepository serviceRepository() {
        return new LocalServiceRepository();
    }

    /**
     * 注册中心服务注册:存在 Spring Cloud 时使用 ServiceRegistry 抽象
     */
    @Bean
    @ConditionalOnClass(ServiceRegistry.class)
    @ConditionalOnMissingBean(RpcServiceRegistry.class)
    public RpcServiceRegistry springCloudServiceRegistry(ObjectProvider<ServiceRegistry<Registration>> serviceRegistryProvider) {
        return new SpringCloudRpcServiceRegistry(serviceRegistryProvider);
    }

    /**
     * 无注册中心时的空实现
     */
    @Bean
    @ConditionalOnMissingBean(RpcServiceRegistry.class)
    public RpcServiceRegistry noopRpcServiceRegistry() {
        return new NoopRpcServiceRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcRequestHandler rpcRequestHandler(ServiceRepository serviceRepository, MessageSerialize messageSerialize) {
        return new RpcRequestHandler(serviceRepository, messageSerialize);
    }

    @Bean
    @ConditionalOnMissingBean
    public NettyRpcServer rpcServer(RpcProperties properties, RpcRequestHandler rpcRequestHandler, MessageSerialize messageSerialize) {
        return new NettyRpcServer(properties.getServer().getPort(), rpcRequestHandler, messageSerialize);
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcServiceExporter rpcServiceExporter(ApplicationContext applicationContext,
                                                 ServiceRepository serviceRepository,
                                                 RpcServiceRegistry rpcServiceRegistry,
                                                 NettyRpcServer rpcServer,
                                                 RpcProperties properties) {
        return new RpcServiceExporter(applicationContext, serviceRepository, rpcServiceRegistry, rpcServer, properties);
    }

    // ==================== 客户端 ====================

    /**
     * 服务发现:存在 Spring Cloud 时使用 DiscoveryClient / LoadBalancerClient 抽象
     */
    @Bean
    @ConditionalOnClass(DiscoveryClient.class)
    @ConditionalOnMissingBean(RpcServiceDiscovery.class)
    public RpcServiceDiscovery springCloudServiceDiscovery(RpcProperties properties,
                                                           ObjectProvider<DiscoveryClient> discoveryClientProvider,
                                                           ObjectProvider<LoadBalancerClient> loadBalancerClientProvider) {
        return new SpringCloudServiceDiscovery(properties, discoveryClientProvider, loadBalancerClientProvider);
    }

    /**
     * 无 Spring Cloud 时的服务发现:优先直连地址,否则调用时报错
     */
    @Bean
    @ConditionalOnMissingBean(RpcServiceDiscovery.class)
    public RpcServiceDiscovery defaultServiceDiscovery(RpcProperties properties) {
        if (StringUtils.hasText(properties.getClient().getDirectAddress())) {
            return new DirectAddressServiceDiscovery(properties.getClient().getDirectAddress());
        }
        return new NoRegistryServiceDiscovery();
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcClient rpcClient(RpcProperties properties, RpcServiceDiscovery rpcServiceDiscovery, MessageSerialize messageSerialize) {
        return new RpcClient(properties, rpcServiceDiscovery, messageSerialize);
    }

}

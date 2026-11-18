package io.github.joinrpc.spring.boot;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.client.NettyRpcClient;
import io.github.joinrpc.core.cluster.LoadBalancer;
import io.github.joinrpc.core.discovery.CachingServiceDiscovery;
import io.github.joinrpc.core.discovery.CompositeServiceDiscovery;
import io.github.joinrpc.core.discovery.ServiceDiscovery;
import io.github.joinrpc.core.discovery.StaticServiceDiscovery;
import io.github.joinrpc.core.proxy.RpcInvoker;
import io.github.joinrpc.core.serialize.Serializers;
import io.github.joinrpc.spring.cloud.SpringCloudSupport;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * join-rpc 自动配置。客户端相关的 Bean 都是 @Lazy 的：只提供服务、不调用别人的应用不会创建客户端线程。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "join-rpc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RpcProperties.class)
public class JoinRpcAutoConfiguration {

    static final String DISCOVERY_CLIENT_CLASS = "org.springframework.cloud.client.discovery.DiscoveryClient";

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static RpcReferenceBeanPostProcessor rpcReferenceBeanPostProcessor() {
        return new RpcReferenceBeanPostProcessor();
    }

    @Bean
    @ConditionalOnProperty(prefix = "join-rpc.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    RpcServiceExporter rpcServiceExporter(RpcProperties properties) {
        return new RpcServiceExporter(properties.getServer());
    }

    @Bean(destroyMethod = "close")
    @Lazy
    @ConditionalOnMissingBean
    NettyRpcClient rpcClient(RpcProperties properties) {
        return new NettyRpcClient(properties.getClient().getConnectTimeout());
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    LoadBalancer rpcLoadBalancer(RpcProperties properties) {
        return LoadBalancer.of(properties.getClient().getLoadBalancer());
    }

    /**
     * 先查直连配置，再查 Spring Cloud DiscoveryClient（classpath 上有 spring-cloud-commons 时）。
     */
    @Bean
    @Lazy
    @ConditionalOnMissingBean
    ServiceDiscovery rpcServiceDiscovery(RpcProperties properties, ListableBeanFactory beanFactory) {
        List<ServiceDiscovery> chain = new ArrayList<>();
        Map<String, List<RpcAddress>> direct = new LinkedHashMap<>();
        properties.getClient().getDirect().forEach((app, addresses) ->
                direct.put(app, addresses.stream().map(RpcAddress::parse).toList()));
        chain.add(new StaticServiceDiscovery(direct));
        if (ClassUtils.isPresent(DISCOVERY_CLIENT_CLASS, getClass().getClassLoader())) {
            chain.add(new CachingServiceDiscovery(SpringCloudSupport.discovery(beanFactory),
                    properties.getClient().getDiscoveryCacheTtl()));
        }
        return new CompositeServiceDiscovery(chain);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    RpcInvoker rpcInvoker(NettyRpcClient client, ServiceDiscovery discovery, LoadBalancer loadBalancer,
                          RpcProperties properties) {
        return new RpcInvoker(client, discovery, loadBalancer, Serializers.get(properties.getSerializer()),
                properties.getClient().getTimeout());
    }
}

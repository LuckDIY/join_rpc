package io.github.chaoleiwang.rpc.server.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.net.URI;

/**
 * 基于 Spring Cloud ServiceRegistry 抽象的服务注册实现。
 * <p>
 * 使用方引入任意注册中心 starter(Nacos / Eureka / Consul / Zookeeper)后,
 * 容器中会存在对应的 {@code ServiceRegistry} Bean,本适配器自动生效,核心库无需感知具体注册中心。
 */
@Slf4j
public class SpringCloudRpcServiceRegistry implements RpcServiceRegistry {

    private final ObjectProvider<ServiceRegistry<Registration>> serviceRegistryProvider;

    public SpringCloudRpcServiceRegistry(ObjectProvider<ServiceRegistry<Registration>> serviceRegistryProvider) {
        this.serviceRegistryProvider = serviceRegistryProvider;
    }

    @Override
    public void register(RpcRegistration registration) {
        ServiceRegistry<Registration> serviceRegistry = serviceRegistryProvider.getIfAvailable();
        if (serviceRegistry == null) {
            log.warn("classpath 上存在 Spring Cloud,但未找到 ServiceRegistry Bean,跳过服务[{}]的远程注册(仅支持直连)",
                    registration.getServiceName());
            return;
        }
        serviceRegistry.register(new RpcSpringRegistration(registration));
        log.info("服务[{}]已注册到注册中心: {}:{}", registration.getServiceName(), registration.getHost(), registration.getPort());
    }

    @Override
    public void deregister(RpcRegistration registration) {
        ServiceRegistry<Registration> serviceRegistry = serviceRegistryProvider.getIfAvailable();
        if (serviceRegistry == null) {
            return;
        }
        serviceRegistry.deregister(new RpcSpringRegistration(registration));
        log.info("服务[{}]已从注册中心注销", registration.getServiceName());
    }

    /**
     * RpcRegistration -> Spring Cloud Registration 适配
     */
    private static class RpcSpringRegistration implements Registration {

        private final RpcRegistration registration;

        RpcSpringRegistration(RpcRegistration registration) {
            this.registration = registration;
        }

        @Override
        public String getServiceId() {
            return registration.getServiceName();
        }

        @Override
        public String getHost() {
            return registration.getHost();
        }

        @Override
        public int getPort() {
            return registration.getPort();
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getInstanceId() {
            return registration.getServiceName() + ":" + registration.getHost() + ":" + registration.getPort();
        }

        @Override
        public URI getUri() {
            return URI.create("rpc://" + registration.getHost() + ":" + registration.getPort());
        }

        @Override
        public java.util.Map<String, String> getMetadata() {
            return registration.getMetadata();
        }

    }

}

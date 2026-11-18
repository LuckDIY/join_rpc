package io.github.joinrpc.spring.cloud;

import io.github.joinrpc.core.discovery.ServiceDiscovery;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * 隔离对 spring-cloud-commons 的引用：只有 classpath 上存在 Spring Cloud 时才会加载这个类。
 */
public final class SpringCloudSupport {

    private SpringCloudSupport() {
    }

    public static ServiceDiscovery discovery(ListableBeanFactory beanFactory) {
        return new SpringCloudServiceDiscovery(beanFactory.getBeanProvider(DiscoveryClient.class));
    }
}

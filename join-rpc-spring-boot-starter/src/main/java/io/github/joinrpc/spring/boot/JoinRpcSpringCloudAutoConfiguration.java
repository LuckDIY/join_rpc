package io.github.joinrpc.spring.boot;

import io.github.joinrpc.spring.cloud.RegistrationMetadataContributor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 有 Spring Cloud 时，把本节点的 RPC 端口写进注册中心实例的 metadata，让调用方能通过 DiscoveryClient 找到它。
 */
@AutoConfiguration(after = JoinRpcAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.cloud.client.serviceregistry.Registration")
@ConditionalOnProperty(prefix = "join-rpc", name = {"enabled", "server.enabled"}, havingValue = "true", matchIfMissing = true)
public class JoinRpcSpringCloudAutoConfiguration {

    @Bean
    RegistrationMetadataContributor joinRpcRegistrationMetadataContributor(ObjectProvider<Registration> registrations,
                                                                           RpcProperties properties,
                                                                           ApplicationContext applicationContext) {
        return new RegistrationMetadataContributor(registrations, properties.getServer().getPort(), applicationContext);
    }
}

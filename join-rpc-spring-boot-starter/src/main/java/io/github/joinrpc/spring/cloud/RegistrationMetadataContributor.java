package io.github.joinrpc.spring.cloud;

import io.github.joinrpc.spring.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationContext;

/**
 * 在所有单例创建完成后、Web 服务器启动（从而触发 Spring Cloud 自动注册）之前，
 * 把 RPC 端口写进本实例的注册 metadata。
 */
public class RegistrationMetadataContributor implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(RegistrationMetadataContributor.class);

    private final ObjectProvider<Registration> registrations;
    private final int port;
    private final ApplicationContext applicationContext;

    public RegistrationMetadataContributor(ObjectProvider<Registration> registrations, int port,
                                           ApplicationContext applicationContext) {
        this.registrations = registrations;
        this.port = port;
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (applicationContext.getBeanNamesForAnnotation(RpcService.class).length == 0) {
            return;
        }
        if (port <= 0) {
            log.warn("join-rpc.server.port={} cannot be published to the registry, use a fixed port", port);
            return;
        }
        boolean[] found = {false};
        registrations.orderedStream().forEach(registration -> {
            found[0] = true;
            try {
                registration.getMetadata().put(RpcMetadata.PORT, String.valueOf(port));
                log.info("join-rpc port {} added to registration metadata of '{}'", port, registration.getServiceId());
            } catch (RuntimeException e) {
                log.warn("cannot modify registration metadata of {} ({}), please configure metadata '{}={}' manually",
                        registration.getServiceId(), e.toString(), RpcMetadata.PORT, port);
            }
        });
        if (!found[0]) {
            log.debug("no Spring Cloud Registration bean, join-rpc port not published to any registry");
        }
    }
}

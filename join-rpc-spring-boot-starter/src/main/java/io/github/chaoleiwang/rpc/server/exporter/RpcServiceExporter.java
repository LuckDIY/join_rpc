package io.github.chaoleiwang.rpc.server.exporter;

import io.github.chaoleiwang.rpc.annotation.RpcService;
import io.github.chaoleiwang.rpc.config.RpcProperties;
import io.github.chaoleiwang.rpc.server.RpcServer;
import io.github.chaoleiwang.rpc.server.registry.RpcRegistration;
import io.github.chaoleiwang.rpc.server.registry.RpcServiceRegistry;
import io.github.chaoleiwang.rpc.server.registry.ServiceObject;
import io.github.chaoleiwang.rpc.server.registry.ServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.ClassUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务导出器(服务端生命周期):
 * <ol>
 *   <li>容器刷新完成后,扫描 Spring 容器中实现了 @RpcService 接口的 Bean;</li>
 *   <li>导出到本地服务仓库;</li>
 *   <li>注册到注册中心(Spring Cloud 抽象);</li>
 *   <li>启动 Netty 服务端。</li>
 * </ol>
 * 容器关闭时按相反顺序注销服务、停止服务端。
 */
@Slf4j
public class RpcServiceExporter implements SmartLifecycle {

    private final ApplicationContext applicationContext;
    private final ServiceRepository serviceRepository;
    private final RpcServiceRegistry rpcServiceRegistry;
    private final RpcServer rpcServer;
    private final RpcProperties properties;

    private final List<RpcRegistration> exportedRegistrations = new CopyOnWriteArrayList<>();

    private volatile boolean running = false;

    public RpcServiceExporter(ApplicationContext applicationContext,
                              ServiceRepository serviceRepository,
                              RpcServiceRegistry rpcServiceRegistry,
                              RpcServer rpcServer,
                              RpcProperties properties) {
        this.applicationContext = applicationContext;
        this.serviceRepository = serviceRepository;
        this.rpcServiceRegistry = rpcServiceRegistry;
        this.rpcServer = rpcServer;
        this.properties = properties;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        if (!properties.getServer().isEnabled()) {
            log.info("rpc.server.enabled=false,跳过服务导出与 RPC 服务端启动(纯消费方模式)");
            running = true;
            return;
        }
        List<RpcRegistration> registrations = exportServices();
        exportedRegistrations.addAll(registrations);
        registrations.forEach(this::safeRegister);
        rpcServer.start();
        running = true;
        log.info("join-rpc 服务端启动完成,共导出 {} 个服务,监听端口 {}", registrations.size(), properties.getServer().getPort());
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        exportedRegistrations.forEach(this::safeDeregister);
        exportedRegistrations.clear();
        serviceRepository.clear();
        rpcServer.stop();
        running = false;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 扫描并导出 @RpcService 服务
     */
    private List<RpcRegistration> exportServices() {
        List<RpcRegistration> registrations = new ArrayList<>();
        String host = resolveHost(properties.getServer().getHost());
        ConfigurableListableBeanFactory beanFactory =
                (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        for (String beanName : applicationContext.getBeanNamesForType(Object.class)) {
            try {
                // 跳过 RpcServiceFactoryBean 创建的消费者代理 Bean
                if (beanFactory.isFactoryBean(beanName)) {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) {
                continue;
            }
            Map<String, Class<?>> services = findRpcServices(beanType);
            if (services.isEmpty()) {
                continue;
            }
            Object bean = applicationContext.getBean(beanName);
            for (Map.Entry<String, Class<?>> entry : services.entrySet()) {
                ServiceObject serviceObject = new ServiceObject(entry.getKey(), entry.getValue(), bean);
                serviceRepository.register(serviceObject);
                registrations.add(buildRegistration(entry.getKey(), host));
                log.info("导出 RPC 服务: {} -> {}", entry.getKey(), beanType.getName());
            }
        }
        return registrations;
    }

    /**
     * 查找 Bean 上标注的 @RpcService:支持标注在实现类本身,或实现类实现的接口上。
     * <p>
     * 注意用 JDK 原生 getAnnotation 而非 Spring AnnotationUtils.findAnnotation:
     * findAnnotation 会沿接口搜索,导致实现类"继承"接口上的注解而重复导出。
     *
     * @return serviceName -> 服务接口
     */
    private Map<String, Class<?>> findRpcServices(Class<?> beanType) {
        Map<String, Class<?>> services = new LinkedHashMap<>();
        RpcService selfAnnotation = beanType.getAnnotation(RpcService.class);
        if (selfAnnotation != null) {
            services.put(resolveServiceName(beanType.getName(), selfAnnotation), beanType);
        }
        for (Class<?> iface : ClassUtils.getAllInterfacesForClass(beanType)) {
            RpcService annotation = iface.getAnnotation(RpcService.class);
            if (annotation != null) {
                services.put(resolveServiceName(iface.getName(), annotation), iface);
            }
        }
        return services;
    }

    private String resolveServiceName(String defaultName, RpcService annotation) {
        String value = annotation.value();
        return (value == null || value.isBlank()) ? defaultName : value;
    }

    private RpcRegistration buildRegistration(String serviceName, String host) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("weight", String.valueOf(properties.getServer().getWeight()));
        metadata.put("group", properties.getGroup());
        metadata.put("protocol", "netty");
        return new RpcRegistration(serviceName, host, properties.getServer().getPort(), metadata);
    }

    private String resolveHost(String configuredHost) {
        if (configuredHost != null && !configuredHost.isBlank()) {
            return configuredHost;
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("获取本机 IP 失败,使用 127.0.0.1", e);
            return "127.0.0.1";
        }
    }

    private void safeRegister(RpcRegistration registration) {
        try {
            rpcServiceRegistry.register(registration);
        } catch (Exception e) {
            log.error("服务[{}]注册到注册中心失败(不影响本地调用)", registration.getServiceName(), e);
        }
    }

    private void safeDeregister(RpcRegistration registration) {
        try {
            rpcServiceRegistry.deregister(registration);
        } catch (Exception e) {
            log.warn("服务[{}]从注册中心注销失败", registration.getServiceName(), e);
        }
    }

}

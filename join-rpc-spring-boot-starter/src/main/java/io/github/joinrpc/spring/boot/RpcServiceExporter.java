package io.github.joinrpc.spring.boot;

import io.github.joinrpc.core.server.NettyRpcServer;
import io.github.joinrpc.core.server.ServiceProviderRegistry;
import io.github.joinrpc.spring.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 收集所有 {@link RpcService} Bean 并启动 RPC 服务端。
 * <p>
 * phase 比 Web 服务器更小：先于 Web 服务器（以及由它触发的注册中心自动注册）启动，晚于它们停止。
 * Spring Cloud 在 ContextClosedEvent 时就从注册中心下线，早于这里的 stop，所以停机时会先下线、再处理完剩余请求、最后关闭端口。
 */
public class RpcServiceExporter implements SmartLifecycle, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(RpcServiceExporter.class);

    public static final int PHASE = SmartLifecycle.DEFAULT_PHASE - 4096;

    private final RpcProperties.Server properties;
    private final ServiceProviderRegistry registry = new ServiceProviderRegistry();
    private ApplicationContext applicationContext;
    private NettyRpcServer server;

    public RpcServiceExporter(RpcProperties.Server properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public synchronized void start() {
        if (server != null) {
            return;
        }
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Class<?> interfaceClass = resolveInterface(entry.getKey(), entry.getValue());
            registry.register((Class) interfaceClass, entry.getValue());
            log.info("join-rpc exporting {} (bean '{}')", interfaceClass.getName(), entry.getKey());
        }
        if (registry.isEmpty()) {
            log.debug("no @RpcService beans, join-rpc server not started");
            return;
        }
        server = new NettyRpcServer(properties.getPort(), registry, properties.getThreads(), properties.getQueueSize());
        server.start();
    }

    static Class<?> resolveInterface(String beanName, Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        RpcService annotation = AnnotatedElementUtils.findMergedAnnotation(targetClass, RpcService.class);
        if (annotation != null && annotation.interfaceClass() != void.class) {
            return annotation.interfaceClass();
        }
        List<Class<?>> candidates = Arrays.stream(ClassUtils.getAllInterfacesForClass(targetClass))
                .filter(i -> !i.getName().startsWith("java.") && !i.getName().startsWith("org.springframework."))
                .toList();
        if (candidates.size() != 1) {
            throw new IllegalStateException("@RpcService bean '" + beanName + "' (" + targetClass.getName()
                    + ") implements " + candidates.size() + " business interfaces " + candidates
                    + ", please specify @RpcService(interfaceClass = ...)");
        }
        return candidates.get(0);
    }

    @Override
    public synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return server != null;
    }

    @Override
    public int getPhase() {
        return PHASE;
    }

    /**
     * @return 实际监听端口，未启动时返回 -1
     */
    public synchronized int getPort() {
        return server == null ? -1 : server.getPort();
    }
}

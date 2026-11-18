package io.github.joinrpc.spring.boot;

import io.github.joinrpc.core.proxy.RpcInvoker;
import io.github.joinrpc.core.proxy.RpcProxyFactory;
import io.github.joinrpc.core.proxy.RpcReferenceConfig;
import io.github.joinrpc.spring.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.util.ReflectionUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 给带 {@link RpcReference} 的字段注入远程代理。在 Bean 初始化方法（@PostConstruct 等）之前完成注入。
 * <p>
 * 代理里的 {@link RpcInvoker} 第一次调用时才从容器中获取，所以这里不会提前初始化网络客户端和注册中心相关的 Bean。
 */
public class RpcReferenceBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private record Key(Class<?> type, String application, long timeout) {
    }

    private final Map<Key, Object> proxies = new ConcurrentHashMap<>();
    private BeanFactory beanFactory;
    private volatile RpcInvoker invoker;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            RpcReference reference = field.getAnnotation(RpcReference.class);
            if (reference == null) {
                return;
            }
            Class<?> type = field.getType();
            if (!type.isInterface()) {
                throw new BeanCreationException(beanName, "@RpcReference field '" + field.getName()
                        + "' must be an interface type, but was " + type.getName());
            }
            String application = resolve(reference.application());
            Object proxy = proxies.computeIfAbsent(new Key(type, application, reference.timeout()), this::createProxy);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, proxy);
        }, field -> field.isAnnotationPresent(RpcReference.class));
        return bean;
    }

    private Object createProxy(Key key) {
        Duration timeout = key.timeout() > 0 ? Duration.ofMillis(key.timeout()) : null;
        RpcReferenceConfig config = new RpcReferenceConfig(key.type(), key.application(), timeout);
        Supplier<RpcInvoker> supplier = () -> {
            RpcInvoker current = invoker;
            if (current == null) {
                current = beanFactory.getBean(RpcInvoker.class);
                invoker = current;
            }
            return current;
        };
        return RpcProxyFactory.create(key.type(), config, supplier);
    }

    private String resolve(String value) {
        if (beanFactory instanceof ConfigurableBeanFactory cbf) {
            String resolved = cbf.resolveEmbeddedValue(value);
            return resolved == null ? value : resolved;
        }
        return value;
    }
}

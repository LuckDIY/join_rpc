package io.github.chaoleiwang.rpc.client;

import io.github.chaoleiwang.rpc.config.RpcProperties;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;

/**
 * 为标注了 @RpcService 的接口生成远程调用代理 Bean。
 * <p>
 * 由 {@code RpcServiceScan} 扫描注册,Bean 名为接口全限定名,并标记为 @Primary:
 * 即使本地存在实现类,按接口类型注入时也优先注入代理(走远程调用),这是有意为之,
 * 让"本地调用"和"远程调用"行为一致;如需本地 Bean 请按实现类类型注入。
 */
public class RpcServiceFactoryBean implements FactoryBean<Object>, ApplicationContextAware {

    private final Class<?> interfaceClass;

    private ApplicationContext applicationContext;

    private volatile Object proxy;

    public RpcServiceFactoryBean(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Override
    public Object getObject() {
        if (proxy == null) {
            synchronized (this) {
                if (proxy == null) {
                    RpcClient rpcClient = applicationContext.getBean(RpcClient.class);
                    RpcProperties properties = applicationContext.getBean(RpcProperties.class);
                    proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                            new RpcServiceProxy(interfaceClass, rpcClient, properties));
                }
            }
        }
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}

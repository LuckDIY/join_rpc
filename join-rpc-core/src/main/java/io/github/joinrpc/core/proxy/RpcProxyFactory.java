package io.github.joinrpc.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * 创建远程服务的 JDK 动态代理。
 */
public final class RpcProxyFactory {

    private RpcProxyFactory() {
    }

    public static <T> T create(Class<T> interfaceClass, RpcReferenceConfig reference, RpcInvoker invoker) {
        return create(interfaceClass, reference, () -> invoker);
    }

    /**
     * @param invoker 延迟获取的调用器，第一次调用远程方法时才会被取用（方便在 Spring 中避免过早初始化）
     */
    public static <T> T create(Class<T> interfaceClass, RpcReferenceConfig reference, Supplier<RpcInvoker> invoker) {
        InvocationHandler handler = new Handler(reference, invoker);
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, handler));
    }

    private record Handler(RpcReferenceConfig reference, Supplier<RpcInvoker> invoker) implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "RpcProxy[" + reference.interfaceClass().getName() + " -> " + reference.application() + "]";
                    default -> throw new UnsupportedOperationException(method.toString());
                };
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, args);
            }
            return invoker.get().invoke(reference, method, args);
        }
    }
}

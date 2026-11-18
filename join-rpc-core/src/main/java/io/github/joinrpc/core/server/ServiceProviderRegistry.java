package io.github.joinrpc.core.server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本节点对外提供的服务：接口名 -> 实现对象。
 */
public class ServiceProviderRegistry {

    private final Map<String, Provider> providers = new ConcurrentHashMap<>();

    public <T> void register(Class<T> interfaceClass, T implementation) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
        }
        if (!interfaceClass.isInstance(implementation)) {
            throw new IllegalArgumentException(implementation.getClass().getName()
                    + " does not implement " + interfaceClass.getName());
        }
        Provider previous = providers.putIfAbsent(interfaceClass.getName(), new Provider(interfaceClass, implementation));
        if (previous != null && previous.implementation() != implementation) {
            throw new IllegalStateException("duplicate provider for " + interfaceClass.getName() + ": "
                    + previous.implementation().getClass().getName() + " and " + implementation.getClass().getName());
        }
    }

    public Provider get(String interfaceName) {
        return providers.get(interfaceName);
    }

    public Collection<Provider> all() {
        return providers.values();
    }

    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * 一个已注册的服务。只允许调用接口上声明的方法。
     */
    public static final class Provider {

        private final Class<?> interfaceClass;
        private final Object implementation;
        private final Map<String, Method> methods = new ConcurrentHashMap<>();

        Provider(Class<?> interfaceClass, Object implementation) {
            this.interfaceClass = interfaceClass;
            this.implementation = implementation;
            for (Method method : interfaceClass.getMethods()) {
                methods.put(signature(method.getName(), typeNames(method.getParameterTypes())), method);
            }
        }

        public Class<?> interfaceClass() {
            return interfaceClass;
        }

        public Object implementation() {
            return implementation;
        }

        public Method findMethod(String name, String[] parameterTypes) {
            return methods.get(signature(name, parameterTypes == null ? new String[0] : parameterTypes));
        }

        static String[] typeNames(Class<?>[] types) {
            return Arrays.stream(types).map(Class::getName).toArray(String[]::new);
        }

        private static String signature(String name, String[] parameterTypes) {
            return name + "(" + String.join(",", parameterTypes) + ")";
        }
    }
}

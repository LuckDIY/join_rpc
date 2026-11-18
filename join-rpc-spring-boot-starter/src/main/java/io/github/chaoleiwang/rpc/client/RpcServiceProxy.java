package io.github.chaoleiwang.rpc.client;

import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.config.RpcProperties;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * RPC 服务代理:把接口方法调用封装成 RpcRequest 交给 RpcClient 执行
 */
public class RpcServiceProxy implements InvocationHandler {

    private final Class<?> interfaceClass;
    private final RpcClient rpcClient;
    private final RpcProperties properties;

    public RpcServiceProxy(Class<?> interfaceClass, RpcClient rpcClient, RpcProperties properties) {
        this.interfaceClass = interfaceClass;
        this.rpcClient = rpcClient;
        this.properties = properties;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return handleObjectMethod(proxy, method, args);
        }
        RpcRequest request = new RpcRequest();
        request.setServiceName(interfaceClass.getName());
        request.setMethodName(method.getName());
        request.setGroup(properties.getGroup());
        request.setParameterTypes(Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new));
        request.setParameters(args);
        return rpcClient.invoke(request, method.getGenericReturnType());
    }

    private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        String name = method.getName();
        if ("toString".equals(name)) {
            return "RpcServiceProxy[interface=" + interfaceClass.getName() + "]";
        }
        if ("hashCode".equals(name)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(name)) {
            return proxy == args[0];
        }
        throw new UnsupportedOperationException("不支持的 Object 方法: " + name);
    }

}

package io.github.chaoleiwang.rpc.server.handler;

import io.github.chaoleiwang.rpc.common.constants.RpcStatusEnum;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.github.chaoleiwang.rpc.server.registry.ServiceObject;
import io.github.chaoleiwang.rpc.server.registry.ServiceRepository;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务端请求处理器:根据请求定位本地服务并反射调用
 */
@Slf4j
public class RpcRequestHandler {

    private final ServiceRepository serviceRepository;

    private final MessageSerialize messageSerialize;

    public RpcRequestHandler(ServiceRepository serviceRepository, MessageSerialize messageSerialize) {
        this.serviceRepository = serviceRepository;
        this.messageSerialize = messageSerialize;
    }

    public RpcResponse handle(RpcRequest request) {
        log.debug("服务端收到请求: {}", request);
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        ServiceObject serviceObject = serviceRepository.get(request.getServiceName());
        if (serviceObject == null) {
            response.setRpcStatus(RpcStatusEnum.NOT_FOUND);
            return response;
        }
        try {
            Class<?>[] parameterTypes = resolveParameterTypes(request.getParameterTypes(), serviceObject.getClazz().getClassLoader());
            Method method = serviceObject.getClazz().getMethod(request.getMethodName(), parameterTypes);
            Object[] parameters = convertParameters(request.getParameters(), parameterTypes);
            Object result = method.invoke(serviceObject.getObj(), parameters);
            response.setReturnValue(result);
            response.setRpcStatus(RpcStatusEnum.SUCCESS);
        } catch (InvocationTargetException e) {
            fillError(response, e.getCause());
        } catch (Exception e) {
            fillError(response, e);
        }
        return response;
    }

    private void fillError(RpcResponse response, Throwable cause) {
        log.error("服务[{}]调用出错", response.getRequestId(), cause);
        response.setRpcStatus(RpcStatusEnum.ERROR);
        response.setErrorMessage(cause != null ? cause.getMessage() : "unknown error");
        response.setErrorClass(cause != null ? cause.getClass().getName() : null);
    }

    /**
     * 参数类型名 -> Class,支持基本类型和数组
     */
    private Class<?>[] resolveParameterTypes(String[] typeNames, ClassLoader classLoader) throws ClassNotFoundException {
        if (typeNames == null || typeNames.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class<?>[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            types[i] = resolveType(typeNames[i], classLoader);
        }
        return types;
    }

    private Class<?> resolveType(String typeName, ClassLoader classLoader) throws ClassNotFoundException {
        if (typeName.endsWith("[]")) {
            Class<?> componentType = resolveType(typeName.substring(0, typeName.length() - 2), classLoader);
            return Array.newInstance(componentType, 0).getClass();
        }
        Class<?> primitive = PrimitiveTypes.forName(typeName);
        if (primitive != null) {
            return primitive;
        }
        try {
            return Class.forName(typeName, false, classLoader);
        } catch (ClassNotFoundException e) {
            // 回退到线程上下文类加载器
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null && contextClassLoader != classLoader) {
                return Class.forName(typeName, false, contextClassLoader);
            }
            throw e;
        }
    }

    /**
     * 参数值类型转换(JSON 树 -> 方法声明类型)
     */
    private Object[] convertParameters(Object[] parameters, Class<?>[] parameterTypes) {
        if (parameters == null) {
            return new Object[0];
        }
        Object[] converted = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            converted[i] = messageSerialize.convertParameter(parameters[i], parameterTypes[i]);
        }
        return converted;
    }

    /**
     * 基本类型名映射
     */
    private enum PrimitiveTypes {
        INT("int", int.class),
        LONG("long", long.class),
        SHORT("short", short.class),
        BYTE("byte", byte.class),
        CHAR("char", char.class),
        BOOLEAN("boolean", boolean.class),
        FLOAT("float", float.class),
        DOUBLE("double", double.class),
        VOID("void", void.class);

        private final String name;
        private final Class<?> type;

        PrimitiveTypes(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        static Class<?> forName(String name) {
            for (PrimitiveTypes value : values()) {
                if (value.name.equals(name)) {
                    return value.type;
                }
            }
            return null;
        }
    }

}

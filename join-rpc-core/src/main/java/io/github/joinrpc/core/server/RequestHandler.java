package io.github.joinrpc.core.server;

import io.github.joinrpc.core.RpcException;
import io.github.joinrpc.core.protocol.JdkCollections;
import io.github.joinrpc.core.protocol.RpcRequest;
import io.github.joinrpc.core.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 根据请求找到本地服务并反射调用。
 */
public class RequestHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private static final Object[] NO_ARGS = new Object[0];

    private final ServiceProviderRegistry registry;

    public RequestHandler(ServiceProviderRegistry registry) {
        this.registry = registry;
    }

    public RpcResponse handle(RpcRequest request) {
        ServiceProviderRegistry.Provider provider = registry.get(request.getInterfaceName());
        if (provider == null) {
            return RpcResponse.error(RpcException.class.getName(),
                    "no provider for " + request.getInterfaceName() + " on this server");
        }
        Method method = provider.findMethod(request.getMethodName(), request.getParameterTypes());
        if (method == null) {
            return RpcResponse.error(RpcException.class.getName(), "no method " + request.getMethodName()
                    + Arrays.toString(request.getParameterTypes()) + " in " + request.getInterfaceName());
        }
        Object[] args = request.getArguments() == null ? NO_ARGS : request.getArguments();
        try {
            return RpcResponse.success(JdkCollections.normalize(method.invoke(provider.implementation(), args)));
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            log.debug("provider {} threw exception", request, target);
            return RpcResponse.error(target);
        } catch (IllegalArgumentException e) {
            return RpcResponse.error(RpcException.class.getName(),
                    "argument mismatch when invoking " + request + ": " + e.getMessage());
        } catch (Exception e) {
            log.warn("failed to invoke {}", request, e);
            return RpcResponse.error(e);
        }
    }
}

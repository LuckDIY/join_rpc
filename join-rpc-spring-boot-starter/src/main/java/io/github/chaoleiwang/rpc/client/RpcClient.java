package io.github.chaoleiwang.rpc.client;

import io.github.chaoleiwang.rpc.client.discovery.RpcServiceDiscovery;
import io.github.chaoleiwang.rpc.client.discovery.SimpleLoadBalancer;
import io.github.chaoleiwang.rpc.common.constants.RpcStatusEnum;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.common.model.RpcServiceInstance;
import io.github.chaoleiwang.rpc.common.serialize.MessageSerialize;
import io.github.chaoleiwang.rpc.config.RpcProperties;
import io.github.chaoleiwang.rpc.exception.RpcException;
import io.github.chaoleiwang.rpc.exception.RpcTransportException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;

/**
 * RPC 客户端调用入口:服务发现 -> 负载均衡 -> 网络调用 -> 结果转换。
 * <p>
 * 传输层异常(RpcTransportException)时自动尝试下一个可用实例,业务异常直接抛出。
 */
@Slf4j
public class RpcClient {

    private final RpcServiceDiscovery serviceDiscovery;
    private final RpcNettyClient nettyClient;
    private final MessageSerialize messageSerialize;
    private final int timeoutMillis;
    private final SimpleLoadBalancer loadBalancer;

    public RpcClient(RpcProperties properties, RpcServiceDiscovery serviceDiscovery, MessageSerialize messageSerialize) {
        this.serviceDiscovery = serviceDiscovery;
        this.nettyClient = new RpcNettyClient(messageSerialize, Math.min(properties.getClient().getTimeoutMillis(), 3000));
        this.messageSerialize = messageSerialize;
        this.timeoutMillis = properties.getClient().getTimeoutMillis();
        this.loadBalancer = new SimpleLoadBalancer(properties.getClient().getLoadBalance());
    }

    /**
     * 发起同步调用
     *
     * @param request    请求
     * @param returnType 方法声明的返回类型(用于反序列化后的类型转换)
     */
    public Object invoke(RpcRequest request, Type returnType) {
        List<RpcServiceInstance> instances = serviceDiscovery.getInstances(request.getServiceName());
        if (instances == null || instances.isEmpty()) {
            throw new RpcException("服务[" + request.getServiceName() + "]没有可用实例");
        }
        List<RpcServiceInstance> ordered = loadBalancer.order(instances, request.getServiceName());

        RpcTransportException lastTransportError = null;
        for (RpcServiceInstance instance : ordered) {
            try {
                RpcResponse response = nettyClient.send(instance.getHost(), instance.getPort(), request, timeoutMillis);
                if (response.getRpcStatus() == RpcStatusEnum.SUCCESS) {
                    return messageSerialize.convertReturnValue(response.getReturnValue(), returnType);
                }
                // 服务端返回的业务错误,不再重试其他实例
                throw new RpcException(buildServerErrorMessage(request, response));
            } catch (RpcTransportException e) {
                lastTransportError = e;
                log.debug("调用实例 {}:{} 失败,尝试下一个实例: {}", instance.getHost(), instance.getPort(), e.getMessage());
            }
        }
        throw new RpcException("RPC 调用失败: " + request.getServiceName() + "#" + request.getMethodName()
                + ", 已尝试 " + ordered.size() + " 个实例", lastTransportError);
    }

    private String buildServerErrorMessage(RpcRequest request, RpcResponse response) {
        String message = response.getErrorMessage() != null
                ? response.getErrorMessage()
                : "rpc status: " + response.getRpcStatus();
        return "调用服务[" + request.getServiceName() + "#" + request.getMethodName() + "]出错: " + message
                + (response.getErrorClass() != null ? " (" + response.getErrorClass() + ")" : "");
    }

    @PreDestroy
    public void shutdown() {
        nettyClient.shutdown();
    }

}

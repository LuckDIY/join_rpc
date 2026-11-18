package io.github.joinrpc.core.proxy;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.RpcException;
import io.github.joinrpc.core.RpcRemoteException;
import io.github.joinrpc.core.RpcTimeoutException;
import io.github.joinrpc.core.client.NettyRpcClient;
import io.github.joinrpc.core.client.RpcConnectException;
import io.github.joinrpc.core.cluster.LoadBalancer;
import io.github.joinrpc.core.discovery.ServiceDiscovery;
import io.github.joinrpc.core.protocol.JdkCollections;
import io.github.joinrpc.core.protocol.RpcRequest;
import io.github.joinrpc.core.protocol.RpcResponse;
import io.github.joinrpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 执行一次远程调用：发现地址 -> 负载均衡 -> 发送 -> 等待响应。
 * <p>
 * 只有连接失败（请求一定没发出去）时才会换节点重试；超时和业务异常不重试，因为请求可能已经执行过。
 * 连接失败的节点会被暂时跳过一段时间（{@link #UNREACHABLE_COOLDOWN}），除非所有节点都不可达。
 */
public class RpcInvoker {

    private static final Logger log = LoggerFactory.getLogger(RpcInvoker.class);

    static final Duration UNREACHABLE_COOLDOWN = Duration.ofSeconds(5);

    private final NettyRpcClient client;
    private final ServiceDiscovery discovery;
    private final LoadBalancer loadBalancer;
    private final Serializer serializer;
    private final Duration defaultTimeout;

    /** 连接失败的地址 -> 冷却结束时间（System.nanoTime） */
    private final Map<RpcAddress, Long> unreachable = new ConcurrentHashMap<>();

    public RpcInvoker(NettyRpcClient client, ServiceDiscovery discovery, LoadBalancer loadBalancer,
                      Serializer serializer, Duration defaultTimeout) {
        this.client = client;
        this.discovery = discovery;
        this.loadBalancer = loadBalancer;
        this.serializer = serializer;
        this.defaultTimeout = defaultTimeout;
    }

    public Object invoke(RpcReferenceConfig reference, Method method, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setInterfaceName(reference.interfaceClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray(String[]::new));
        // 复制一份，避免修改调用方传入的数组
        request.setArguments(args == null ? null : (Object[]) JdkCollections.normalize(args.clone()));

        Duration timeout = reference.timeout() != null ? reference.timeout() : defaultTimeout;
        RpcResponse response = send(reference, request, timeout);
        if (response.isSuccess()) {
            return response.getValue();
        }
        throw new RpcRemoteException(response.getErrorType(), response.getErrorMessage());
    }

    private RpcResponse send(RpcReferenceConfig reference, RpcRequest request, Duration timeout) {
        String application = reference.application();
        List<RpcAddress> candidates = new ArrayList<>(discovery.getAddresses(application));
        if (candidates.isEmpty()) {
            throw new RpcException("no available provider of application '" + application + "' for " + request.getInterfaceName());
        }
        skipUnreachable(candidates);
        RpcConnectException lastConnectError = null;
        while (!candidates.isEmpty()) {
            RpcAddress address = loadBalancer.select(candidates, request);
            try {
                CompletableFuture<RpcResponse> future = client.send(address, request, serializer.id(), timeout);
                unreachable.remove(address);
                return future.get();
            } catch (RpcConnectException e) {
                log.warn("{}, trying next provider of '{}'", e.getMessage(), application);
                lastConnectError = e;
                unreachable.put(address, System.nanoTime() + UNREACHABLE_COOLDOWN.toNanos());
                candidates.remove(address);
                discovery.invalidate(application);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof TimeoutException) {
                    throw new RpcTimeoutException("invoke " + request + " on " + address + " timed out after " + timeout.toMillis() + "ms");
                }
                if (cause instanceof RpcException rpcException) {
                    throw rpcException;
                }
                throw new RpcException("invoke " + request + " on " + address + " failed", cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RpcException("interrupted while invoking " + request, e);
            }
        }
        throw new RpcException("all providers of application '" + application + "' are unreachable", lastConnectError);
    }

    /**
     * 去掉冷却中的地址；如果全部都在冷却中，就保留全部，让它们再试一次。
     */
    private void skipUnreachable(List<RpcAddress> candidates) {
        if (unreachable.isEmpty()) {
            return;
        }
        long now = System.nanoTime();
        unreachable.values().removeIf(until -> now - until >= 0);
        List<RpcAddress> reachable = candidates.stream().filter(a -> !unreachable.containsKey(a)).toList();
        if (!reachable.isEmpty()) {
            candidates.retainAll(reachable);
        }
    }
}

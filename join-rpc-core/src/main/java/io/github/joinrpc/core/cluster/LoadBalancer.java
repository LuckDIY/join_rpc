package io.github.joinrpc.core.cluster;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.protocol.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略。
 */
public interface LoadBalancer {

    /**
     * @param candidates 非空的候选地址列表
     */
    RpcAddress select(List<RpcAddress> candidates, RpcRequest request);

    /**
     * 根据名称创建内置策略：random、round-robin。
     */
    static LoadBalancer of(String name) {
        return switch (name.toLowerCase()) {
            case "random" -> new RandomLoadBalancer();
            case "round-robin", "roundrobin" -> new RoundRobinLoadBalancer();
            default -> throw new IllegalArgumentException("unknown load balancer '" + name + "', available: random, round-robin");
        };
    }
}

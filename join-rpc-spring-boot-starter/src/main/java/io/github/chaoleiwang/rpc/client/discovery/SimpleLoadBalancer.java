package io.github.chaoleiwang.rpc.client.discovery;

import io.github.chaoleiwang.rpc.common.model.RpcServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的客户端负载均衡:random(默认) / roundRobin。
 * <p>
 * 只负责把"选中的实例"排到候选列表首位,剩下的实例保留在后面作为故障转移的备选。
 */
public class SimpleLoadBalancer {

    private static final String STRATEGY_ROUND_ROBIN = "roundrobin";

    private final String strategy;

    private final ConcurrentHashMap<String, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();

    private final Random random = new Random();

    public SimpleLoadBalancer(String strategy) {
        this.strategy = strategy == null ? "" : strategy.trim().toLowerCase();
    }

    /**
     * 排序候选实例:选中的实例在前,其余实例在后
     */
    public List<RpcServiceInstance> order(List<RpcServiceInstance> instances, String serviceName) {
        if (instances == null || instances.size() <= 1) {
            return instances;
        }
        int pickedIndex;
        if (STRATEGY_ROUND_ROBIN.equals(strategy)) {
            AtomicInteger counter = roundRobinCounters.computeIfAbsent(serviceName, key -> new AtomicInteger());
            pickedIndex = Math.floorMod(counter.getAndIncrement(), instances.size());
        } else {
            // 默认 random
            pickedIndex = random.nextInt(instances.size());
        }
        List<RpcServiceInstance> ordered = new ArrayList<>(instances.size());
        ordered.add(instances.get(pickedIndex));
        for (int i = 0; i < instances.size(); i++) {
            if (i != pickedIndex) {
                ordered.add(instances.get(i));
            }
        }
        return ordered;
    }

}

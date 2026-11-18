package io.github.joinrpc.core.cluster;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.protocol.RpcRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public RpcAddress select(List<RpcAddress> candidates, RpcRequest request) {
        return candidates.get(Math.floorMod(counter.getAndIncrement(), candidates.size()));
    }
}

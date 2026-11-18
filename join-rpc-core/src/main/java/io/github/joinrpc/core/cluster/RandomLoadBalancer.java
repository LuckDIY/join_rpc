package io.github.joinrpc.core.cluster;

import io.github.joinrpc.core.RpcAddress;
import io.github.joinrpc.core.protocol.RpcRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public RpcAddress select(List<RpcAddress> candidates, RpcRequest request) {
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }
}

package io.github.joinrpc.core.discovery;

import io.github.joinrpc.core.RpcAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 给底层发现加一层短时缓存。底层查询失败时继续使用上一次的结果，避免注册中心抖动导致调用全部失败。
 */
public class CachingServiceDiscovery implements ServiceDiscovery {

    private static final Logger log = LoggerFactory.getLogger(CachingServiceDiscovery.class);

    private record Entry(List<RpcAddress> addresses, long expireAt) {
    }

    private final ServiceDiscovery delegate;
    private final long ttlNanos;
    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    public CachingServiceDiscovery(ServiceDiscovery delegate, Duration ttl) {
        this.delegate = delegate;
        this.ttlNanos = ttl.toNanos();
    }

    @Override
    public List<RpcAddress> getAddresses(String application) {
        Entry entry = cache.get(application);
        long now = System.nanoTime();
        if (entry != null && now - entry.expireAt() < 0) {
            return entry.addresses();
        }
        try {
            List<RpcAddress> addresses = List.copyOf(delegate.getAddresses(application));
            cache.put(application, new Entry(addresses, now + ttlNanos));
            return addresses;
        } catch (RuntimeException e) {
            if (entry != null) {
                log.warn("failed to refresh addresses of '{}', using stale cache", application, e);
                return entry.addresses();
            }
            throw e;
        }
    }

    @Override
    public void invalidate(String application) {
        cache.remove(application);
        delegate.invalidate(application);
    }
}

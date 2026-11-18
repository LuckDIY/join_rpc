package io.github.chaoleiwang.rpc.server.registry;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于内存 Map 的本地服务仓库实现
 */
@Slf4j
public class LocalServiceRepository implements ServiceRepository {

    private final Map<String, ServiceObject> serviceMap = new ConcurrentHashMap<>();

    @Override
    public void register(ServiceObject serviceObject) {
        if (serviceObject == null || serviceObject.getName() == null) {
            throw new IllegalArgumentException("ServiceObject 及其 name 不能为空");
        }
        ServiceObject previous = serviceMap.put(serviceObject.getName(), serviceObject);
        if (previous != null) {
            log.warn("本地服务[{}]被覆盖: {} -> {}", serviceObject.getName(), previous.getObj().getClass().getName(),
                    serviceObject.getObj().getClass().getName());
        }
    }

    @Override
    public ServiceObject get(String serviceName) {
        return serviceMap.get(serviceName);
    }

    @Override
    public void clear() {
        serviceMap.clear();
    }

}

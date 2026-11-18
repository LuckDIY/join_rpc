package io.github.chaoleiwang.rpc.server.registry;

/**
 * 本地服务仓库:服务端执行请求时按服务名查找本地服务对象
 */
public interface ServiceRepository {

    /**
     * 注册本地服务对象
     */
    void register(ServiceObject serviceObject);

    /**
     * 按服务名获取本地服务对象
     */
    ServiceObject get(String serviceName);

    /**
     * 清空所有本地服务
     */
    void clear();

}

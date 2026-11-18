package io.github.chaoleiwang.rpc.server.registry;

/**
 * 服务注册接口:把本节点提供的服务注册到注册中心,不绑定具体注册中心实现。
 * <p>
 * 默认实现基于 Spring Cloud 的 {@code ServiceRegistry} 抽象(Nacos/Eureka 等注册中心
 * 由使用方自行引入对应 starter 提供实现);没有注册中心时使用 Noop 实现,仅支持直连调用。
 */
public interface RpcServiceRegistry {

    /**
     * 注册服务
     */
    void register(RpcRegistration registration) throws Exception;

    /**
     * 注销服务
     */
    void deregister(RpcRegistration registration) throws Exception;

}

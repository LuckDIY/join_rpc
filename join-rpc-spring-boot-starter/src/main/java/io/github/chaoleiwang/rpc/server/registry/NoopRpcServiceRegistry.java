package io.github.chaoleiwang.rpc.server.registry;

import lombok.extern.slf4j.Slf4j;

/**
 * 空实现:classpath 上没有 Spring Cloud 时的默认服务注册。
 * <p>
 * 此时服务不会注册到任何注册中心,消费方需通过 rpc.client.direct-address 直连调用。
 */
@Slf4j
public class NoopRpcServiceRegistry implements RpcServiceRegistry {

    @Override
    public void register(RpcRegistration registration) {
        log.info("未接入注册中心,跳过服务[{}]的远程注册(仅支持 rpc.client.direct-address 直连)", registration.getServiceName());
    }

    @Override
    public void deregister(RpcRegistration registration) {
        // no-op
    }

}

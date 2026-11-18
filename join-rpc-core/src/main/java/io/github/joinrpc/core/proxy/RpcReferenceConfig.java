package io.github.joinrpc.core.proxy;

import java.time.Duration;
import java.util.Objects;

/**
 * 一个远程服务引用的配置。
 *
 * @param interfaceClass 服务接口
 * @param application    提供方应用名
 * @param timeout        调用超时，null 表示使用全局默认值
 */
public record RpcReferenceConfig(Class<?> interfaceClass, String application, Duration timeout) {

    public RpcReferenceConfig {
        Objects.requireNonNull(interfaceClass, "interfaceClass");
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
        }
        if (application == null || application.isBlank()) {
            throw new IllegalArgumentException("application of " + interfaceClass.getName() + " must not be blank");
        }
    }
}

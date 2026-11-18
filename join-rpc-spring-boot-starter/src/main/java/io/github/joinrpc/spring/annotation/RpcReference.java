package io.github.joinrpc.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在字段上，注入远程服务的代理。
 *
 * <pre>
 * &#64;RpcReference(application = "order-service")
 * private OrderService orderService;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {

    /**
     * 提供方应用名，即它在注册中心里的 serviceId（通常是提供方的 spring.application.name）。
     * 支持 ${...} 占位符。
     */
    String application();

    /**
     * 调用超时（毫秒），0 表示使用 join-rpc.client.timeout。
     */
    long timeout() default 0;
}

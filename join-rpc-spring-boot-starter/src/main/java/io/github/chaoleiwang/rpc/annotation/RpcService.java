package io.github.chaoleiwang.rpc.annotation;

import java.lang.annotation.*;

/**
 * RPC 服务标识,标注在接口上。
 * <p>
 * 服务提供方:实现类实现该接口后,启动时自动导出并注册到注册中心(服务名默认为接口全限定名);
 * 服务消费方:配合 {@link RpcServiceScan} 使用,该接口会被自动生成为远程调用代理 Bean,可直接 @Autowired 注入。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {

    /**
     * 服务发布名称,默认取接口全限定名。
     * 一个实现类实现多个 @RpcService 接口时,每个接口按各自的名字分别注册。
     */
    String value() default "";

}

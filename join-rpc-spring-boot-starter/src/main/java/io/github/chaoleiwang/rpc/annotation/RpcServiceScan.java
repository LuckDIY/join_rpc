package io.github.chaoleiwang.rpc.annotation;

import io.github.chaoleiwang.rpc.scan.RpcServiceScanImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启 RPC 服务扫描,标注在启动类或配置类上。
 * <p>
 * 扫描 basePackages 下标注了 {@link RpcService} 的接口,为没有本地实现类的接口注册远程调用代理 Bean。
 * 不指定扫描路径时,默认扫描标注类所在的包。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcServiceScanImportBeanDefinitionRegistrar.class)
public @interface RpcServiceScan {

    /**
     * 扫描包路径
     */
    @org.springframework.core.annotation.AliasFor("basePackages")
    String[] value() default {};

    /**
     * 扫描包路径(value 的别名)
     */
    @org.springframework.core.annotation.AliasFor("value")
    String[] basePackages() default {};

}

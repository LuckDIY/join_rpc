package com.example.join_rpc.annotation;

import com.example.join_rpc.scan.RpcServiceScanImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 提供服务标识
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcServiceScanImportBeanDefinitionRegistrar.class)
public @interface RpcServiceScan {

    /**
     * 扫描路径
     * @return
     */
    String value() default "";
}

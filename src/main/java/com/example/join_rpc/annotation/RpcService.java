package com.example.join_rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 提供服务标识
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
//注册bean
@Component
@Documented
public @interface RpcService {

    /**
     * 服务发布名称 实现多接口时按注解设置的值发布服务
     * 单实现 无效
     */
    String value() default "";

    /**
     * 服务分组 服务接口有多个实现类时按注解设置的值分组
     */
    String group() default "";

    /**
     * 服务版本
     */
    String version() default "";
}

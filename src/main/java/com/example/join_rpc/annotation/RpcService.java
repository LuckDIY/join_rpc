package com.example.join_rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 提供服务标识
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {

    /**
     * 服务发布名称 实现多接口时按注解设置的值发布服务
     * 单实现 无效
     */
    String value() default "";

}

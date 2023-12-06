package com.example.join_rpc.annotation;

import java.lang.annotation.*;

/**
 * 代理处理器注解
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcProxyHandleAno {
    String value() default "";
}

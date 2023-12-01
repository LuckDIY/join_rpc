package com.example.join_rpc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcProcessorAno {


    //代理类型
    public String value() default "";
}

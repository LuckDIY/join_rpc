package io.github.joinrpc.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在服务<b>实现类</b>上，把它作为 RPC 服务对外提供。同时它也是一个普通的 Spring Bean。
 *
 * <pre>
 * &#64;RpcService
 * public class HelloServiceImpl implements HelloService { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {

    /**
     * 对外暴露的接口。实现类只实现了一个业务接口时可以不填；实现了多个时必须指定。
     */
    Class<?> interfaceClass() default void.class;
}

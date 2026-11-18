package io.github.chaoleiwang.example.provider;

import io.github.chaoleiwang.rpc.annotation.RpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务提供方应用(spring-boot:run 默认启动)
 */
@RpcServiceScan("io.github.chaoleiwang.example.api")
@SpringBootApplication
public class ExampleProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleProviderApplication.class, args);
    }

}

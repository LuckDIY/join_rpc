package io.github.chaoleiwang.example.consumer;

import io.github.chaoleiwang.rpc.annotation.RpcServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务消费方应用
 */
@RpcServiceScan("io.github.chaoleiwang.example.api")
@SpringBootApplication
public class ExampleConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleConsumerApplication.class, args);
    }

}

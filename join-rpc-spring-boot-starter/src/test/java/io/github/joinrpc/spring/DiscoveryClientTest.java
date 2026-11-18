package io.github.joinrpc.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 通过 Spring Cloud 标准 DiscoveryClient 发现提供方。
 * 这里用 spring-cloud-commons 自带的 SimpleDiscoveryClient（配置文件里写死实例），
 * 换成 Nacos/Consul/Eureka 时走的是完全相同的代码路径。
 * <p>
 * greeting-app 有两个实例：一个是普通 HTTP 服务（没有 join-rpc.port metadata，应被忽略），一个是 join-rpc 提供方。
 */
@SpringBootTest
class DiscoveryClientTest {

    static final int PORT = SpringRpcTestSupport.freePort();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("join-rpc.server.port", () -> PORT);
        registry.add("test.provider-app", () -> "greeting-app");
        registry.add("spring.cloud.discovery.client.simple.instances.greeting-app[0].uri", () -> "http://127.0.0.1:1");
        registry.add("spring.cloud.discovery.client.simple.instances.greeting-app[1].uri", () -> "http://127.0.0.1:8080");
        registry.add("spring.cloud.discovery.client.simple.instances.greeting-app[1].metadata.join-rpc.port", () -> PORT);
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({TestServices.GreetingServiceImpl.class, TestServices.GreetingClient.class})
    static class App {
    }

    @Autowired
    TestServices.GreetingClient client;

    @Test
    void remoteCallThroughDiscoveryClient() {
        for (int i = 0; i < 20; i++) {
            assertThat(client.greetingService.greet("cloud")).isEqualTo("hello cloud");
        }
    }
}

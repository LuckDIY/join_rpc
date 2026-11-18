package io.github.joinrpc.spring;

import io.github.joinrpc.core.RpcException;
import io.github.joinrpc.spring.annotation.RpcReference;
import io.github.joinrpc.spring.boot.RpcServiceExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 同一个应用里既提供 GreetingService，又通过直连地址远程调用它。
 */
@SpringBootTest(properties = "spring.cloud.discovery.enabled=false")
class DirectAddressTest {

    static final int PORT = SpringRpcTestSupport.freePort();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("join-rpc.server.port", () -> PORT);
        registry.add("test.provider-app", () -> "greeting-app");
        registry.add("join-rpc.client.direct.greeting-app", () -> "127.0.0.1:" + PORT);
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({TestServices.GreetingServiceImpl.class, TestServices.GreetingClient.class, UnknownAppClient.class})
    static class App {
    }

    @Component
    static class UnknownAppClient {
        @RpcReference(application = "unknown-app")
        TestServices.GreetingService greetingService;
    }

    @Autowired
    TestServices.GreetingClient client;

    @Autowired
    UnknownAppClient unknownAppClient;

    @Autowired
    RpcServiceExporter exporter;

    @Test
    void remoteCallThroughDirectAddress() {
        assertThat(exporter.getPort()).isEqualTo(PORT);
        assertThat(client.injectedBeforeInit).isTrue();
        assertThat(client.greetingService.greet("join")).isEqualTo("hello join");
    }

    @Test
    void unknownApplicationFailsClearly() {
        assertThatThrownBy(() -> unknownAppClient.greetingService.greet("x"))
                .isInstanceOf(RpcException.class)
                .hasMessageContaining("unknown-app");
    }
}

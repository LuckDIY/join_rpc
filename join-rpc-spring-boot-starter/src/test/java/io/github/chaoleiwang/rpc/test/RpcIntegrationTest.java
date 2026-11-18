package io.github.chaoleiwang.rpc.test;

import io.github.chaoleiwang.rpc.annotation.RpcServiceScan;
import io.github.chaoleiwang.rpc.test.api.HelloService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试:同一进程内启动 RPC 服务端,通过直连地址完成完整的
 * 代理 -> Netty 客户端 -> Netty 服务端 -> 实现类 的远程调用链路
 */
@SpringBootTest(classes = RpcIntegrationTest.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "rpc.server.port=19101",
                "rpc.client.direct-address=127.0.0.1:19101"
        })
class RpcIntegrationTest {

    @SpringBootApplication
    @RpcServiceScan("io.github.chaoleiwang.rpc.test.api")
    static class TestApp {
    }

    @Autowired
    private HelloService helloService;

    @Test
    void remoteCallRoundTrip() {
        assertThat(helloService.hello("join-rpc")).isEqualTo("hello join-rpc");
        assertThat(helloService.hello("join-rpc", 18)).isEqualTo("join-rpc18");
        assertThat(helloService.helloInteger(21)).isEqualTo(42);
        assertThat(helloService.helloInt(5)).isEqualTo(6);
    }

}

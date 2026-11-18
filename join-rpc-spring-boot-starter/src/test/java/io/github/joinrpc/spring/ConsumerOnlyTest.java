package io.github.joinrpc.spring;

import io.github.joinrpc.core.client.NettyRpcClient;
import io.github.joinrpc.spring.boot.RpcServiceExporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 没有 @RpcService 时不监听端口；没有发生远程调用时不创建客户端。
 */
@SpringBootTest(properties = "test.provider-app=nobody")
class ConsumerOnlyTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(TestServices.GreetingClient.class)
    static class App {
    }

    @Autowired
    RpcServiceExporter exporter;

    @Autowired
    ApplicationContext context;

    @Test
    void noServerAndLazyClient() {
        assertThat(exporter.isRunning()).isFalse();
        assertThat(exporter.getPort()).isEqualTo(-1);
        var beanFactory = (org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
                context.getAutowireCapableBeanFactory();
        assertThat(beanFactory.containsSingleton("rpcClient")).isFalse();
        assertThat(context.getBeanNamesForType(NettyRpcClient.class, false, false)).isNotEmpty();
    }
}

package com.example.join_rpc.config;

import com.example.join_rpc.annotation.RpcProcessorAno;
import com.example.join_rpc.server.register.DefaultRpcBaseProcessor;
import com.example.join_rpc.server.register.ServerRegister;
import com.example.join_rpc.server.register.ZookeeperServerRegister;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.ServiceLoader;


@Configuration
//注册配置属性
@EnableConfigurationProperties(RpcConfig.class)
//缺失配置默认认为是true
@ConditionalOnProperty(prefix = "rpc", name = "enable", havingValue = "true", matchIfMissing = true)
public class RpcAutoConfiguration {


    @PostConstruct
    public void extensionLoader() {
        System.out.println("123456");
    }

    @Bean
    public ServerRegister serverRegister(@Autowired RpcConfig rpcConfig) {

        return new ZookeeperServerRegister(rpcConfig.getRegisterAddress(),
                rpcConfig.getServerPort(), rpcConfig.getProtocol(),
                rpcConfig.isEnableCompress() ? rpcConfig.getCompress() : "UNZIP",
                rpcConfig.getWeight());
    }


    /**
     * rpc处理程序，负责注册服务，启动服务
     *
     * @param serverRegister
     * @return
     */
    @Bean
    public DefaultRpcBaseProcessor rpcProcessor(RpcConfig rpcConfig, ServerRegister serverRegister) {
        DefaultRpcBaseProcessor defaultRpcBaseProcessor = getDefaultRpcBaseProcessor(rpcConfig.getServerProxyType());
        defaultRpcBaseProcessor.initializeParameters(serverRegister);
        return defaultRpcBaseProcessor;
    }


    /**
     * 根据服务代理类型，获取代理服务处理器
     *
     * @param serverProxyType
     * @return
     */
    public DefaultRpcBaseProcessor getDefaultRpcBaseProcessor(String serverProxyType) {
        ServiceLoader<DefaultRpcBaseProcessor> processors = ServiceLoader.load(DefaultRpcBaseProcessor.class);

        for (DefaultRpcBaseProcessor processor : processors) {
            RpcProcessorAno annotation = processor.getClass().getAnnotation(RpcProcessorAno.class);
            Assert.notNull(annotation, "load default rpc base processor can not be empty!");
            if (annotation.value().equals(serverProxyType)) {
                return processor;
            }
        }
        throw new RuntimeException("无匹配的rpc代理处理器");
    }
}

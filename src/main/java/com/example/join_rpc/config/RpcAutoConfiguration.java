package com.example.join_rpc.config;

import com.example.join_rpc.register.DefaultRpcBaseProcessor;
import com.example.join_rpc.register.ServerRegister;
import com.example.join_rpc.register.ZookeeperServerRegister;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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

    @Bean
    public DefaultRpcBaseProcessor defaultRpcBaseProcessor(ServerRegister serverRegister) {
        return new DefaultRpcBaseProcessor(serverRegister);
    }
}

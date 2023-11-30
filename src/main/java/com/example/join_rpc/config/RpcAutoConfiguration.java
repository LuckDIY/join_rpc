package com.example.reg_center.config;

import com.example.reg_center.server.register.ServerRegister;
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
@ConditionalOnProperty(prefix = "rpc",name = "enable",havingValue = "true",matchIfMissing = true)
public class RpcAutoConfiguration {



    @PostConstruct
    public void extensionLoader() {
        System.out.println("123456");
    }

    @Bean
    public ServerRegister serverRegister(@Autowired RpcConfig rpcConfig) {
        return new ZookeeperServerRegister(rpcConfig.getRegisterAddress(), rpcConfig.getServerPort(), rpcConfig.getProtocol(), rpcConfig.isEnableCompress() ? rpcConfig.getCompress() : RpcCompressEnum.UNZIP.getCompress(), rpcConfig.getWeight());
    }
}

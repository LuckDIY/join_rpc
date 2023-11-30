package com.example.join_rpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties("rpc")
public class RpcConfig {

    /**
     * 是否启用rpc 默认启用
     */
    private boolean enable = true;

    /**
     * 注册中心地址
     */
    private String registerAddress = "127.0.0.1:2128";

    /**
     * rpc服务暴露端口
     */
    private Integer serverPort = 9999;

    /**
     * 服务协议 todo 这个序列化协议需要研究下
     */
    private String protocol = "kryo";

    /**
     * 压缩算法
     */
    private String compress = "Gzip";

    /**
     * 服务是否启用压缩算法
     */
    private boolean enableCompress = false;

    /**
     * Netty客户端是否使用连接池
     */
    private boolean enableNettyChannelPool = true;

    /**
     * Netty连接池中的最大连接数
     */
    private Integer nettyChannelPoolMaxConnections = 20;

    /**
     * Netty连接池获取连接超时时是否创建新连接
     */
    private boolean nettyChannelPoolGetNewOnAcquireTimeout = false;

    /**
     * 负载均衡算法
     */
    private String loadBalance = "random";

    /**
     * 权重
     */
    private Integer weight = 1;

    /**
     * 服务代理类型 reflect： 反射调用 javassist： 字节码生成代理类调用
     */
    private String serverProxyType = "javassist";
}

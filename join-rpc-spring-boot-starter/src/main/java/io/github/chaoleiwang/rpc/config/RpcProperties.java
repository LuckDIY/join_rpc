package io.github.chaoleiwang.rpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * join-rpc 配置项,前缀 rpc
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {

    /**
     * 是否启用 rpc,默认启用
     */
    private boolean enabled = true;

    /**
     * 序列化协议: json(默认) / protostuff
     */
    private String serializer = "json";

    /**
     * 服务分组,透传到注册中心元数据
     */
    private String group = "default";

    private Server server = new Server();

    private Client client = new Client();

    @Data
    public static class Server {

        /**
         * 是否启动 RPC 服务端并导出服务,默认开启;纯消费方应用可设为 false
         */
        private boolean enabled = true;

        /**
         * RPC 服务暴露端口
         */
        private int port = 9999;

        /**
         * 注册到注册中心的主机地址,留空自动取本机 IP
         */
        private String host = "";

        /**
         * 服务权重
         */
        private int weight = 100;

    }

    @Data
    public static class Client {

        /**
         * 调用超时时间(毫秒)
         */
        private int timeoutMillis = 5000;

        /**
         * 多实例负载均衡策略: random(默认) / roundRobin
         */
        private String loadBalance = "random";

        /**
         * 直连地址(不经过注册中心时使用),格式 host:port,多个用逗号分隔;
         * 配置了直连地址时优先走直连,否则走注册中心发现
         */
        private String directAddress = "";

    }
}

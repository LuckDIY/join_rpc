package io.github.joinrpc.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * join-rpc 配置，前缀 join-rpc。
 */
@ConfigurationProperties("join-rpc")
public class RpcProperties {

    /** 是否启用 join-rpc */
    private boolean enabled = true;

    /** 序列化方式，内置 protostuff */
    private String serializer = "protostuff";

    private final Server server = new Server();

    private final Client client = new Client();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public Server getServer() {
        return server;
    }

    public Client getClient() {
        return client;
    }

    public static class Server {

        /** 是否启用服务端。即使启用，没有 @RpcService 时也不会监听端口 */
        private boolean enabled = true;

        /** RPC 监听端口。注册到注册中心时会写进实例 metadata 的 join-rpc.port */
        private int port = 9999;

        /** 业务线程数 */
        private int threads = 200;

        /** 业务线程池队列长度，满了之后的请求直接返回 server is busy */
        private int queueSize = 1000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }
    }

    public static class Client {

        /** 默认调用超时 */
        private Duration timeout = Duration.ofSeconds(3);

        /** 建立连接超时 */
        private Duration connectTimeout = Duration.ofSeconds(3);

        /** 负载均衡：random、round-robin */
        private String loadBalancer = "random";

        /** 从注册中心查询到的地址缓存多久 */
        private Duration discoveryCacheTtl = Duration.ofSeconds(5);

        /**
         * 直连地址，优先于注册中心。key 为提供方应用名，value 为 host:port 列表，例如
         * join-rpc.client.direct.order-service=127.0.0.1:9999,127.0.0.1:9998
         */
        private Map<String, List<String>> direct = new LinkedHashMap<>();

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public String getLoadBalancer() {
            return loadBalancer;
        }

        public void setLoadBalancer(String loadBalancer) {
            this.loadBalancer = loadBalancer;
        }

        public Duration getDiscoveryCacheTtl() {
            return discoveryCacheTtl;
        }

        public void setDiscoveryCacheTtl(Duration discoveryCacheTtl) {
            this.discoveryCacheTtl = discoveryCacheTtl;
        }

        public Map<String, List<String>> getDirect() {
            return direct;
        }

        public void setDirect(Map<String, List<String>> direct) {
            this.direct = direct;
        }
    }
}

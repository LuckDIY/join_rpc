package io.github.chaoleiwang.rpc.server.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 待注册到注册中心的服务注册信息(与具体注册中心解耦)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcRegistration {

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 元数据(权重、分组、协议等)
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

}

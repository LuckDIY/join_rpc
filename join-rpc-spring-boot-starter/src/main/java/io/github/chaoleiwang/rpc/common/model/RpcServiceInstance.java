package io.github.chaoleiwang.rpc.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 一个可用的服务实例地址(由注册中心发现或直连地址解析而来)
 */
@Data
@AllArgsConstructor
public class RpcServiceInstance {

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 元数据(权重等)
     */
    private Map<String, String> metadata;

}

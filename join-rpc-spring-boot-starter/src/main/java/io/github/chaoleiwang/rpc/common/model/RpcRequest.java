package io.github.chaoleiwang.rpc.common.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 请求
 */
@Data
public class RpcRequest {

    /**
     * 请求 id
     */
    private int requestId;

    /**
     * 服务名(接口全限定名)
     */
    private String serviceName;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 分组
     */
    private String group;

    /**
     * 头信息
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 参数类型(全限定名,便于跨语言/跨序列化协议传输)
     */
    private String[] parameterTypes;

    /**
     * 参数值
     */
    private Object[] parameters;

}

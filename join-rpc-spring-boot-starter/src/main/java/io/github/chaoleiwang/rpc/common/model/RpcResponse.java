package io.github.chaoleiwang.rpc.common.model;

import io.github.chaoleiwang.rpc.common.constants.RpcStatusEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 响应
 */
@Data
public class RpcResponse {

    /**
     * 请求 id
     */
    private int requestId;

    /**
     * 头信息
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * 返回值
     */
    private Object returnValue;

    /**
     * 服务端异常信息(不为空说明执行出错)
     */
    private String errorMessage;

    /**
     * 服务端异常类名
     */
    private String errorClass;

    /**
     * RPC 状态
     */
    private RpcStatusEnum rpcStatus;

    public RpcResponse() {
    }

    public RpcResponse(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus;
    }

}

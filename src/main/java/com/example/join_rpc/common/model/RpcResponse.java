package com.example.join_rpc.common.model;

import com.example.join_rpc.common.constants.RpcStatusEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RpcResponse {

    //请求id
    private String requestId;

    //头信息
    private Map<String, String> headers = new HashMap<>();

    //返回值
    private Object returnValue;

    //异常
    private Exception exception;

    //rpc状态
    private RpcStatusEnum rpcStatus;

    public RpcResponse(RpcStatusEnum rpcStatus) {
        this.rpcStatus = rpcStatus;
    }

    public RpcResponse() {
    }
}

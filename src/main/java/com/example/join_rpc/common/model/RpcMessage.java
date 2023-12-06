package com.example.join_rpc.common.model;

import lombok.Data;

@Data
public class RpcMessage {


    /**
     * 类型 1 请求 2 响应 3 心跳 4 心跳响应
     */
    private byte type;


    private Object data;
}

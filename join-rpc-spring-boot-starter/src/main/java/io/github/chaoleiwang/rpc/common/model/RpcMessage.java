package io.github.chaoleiwang.rpc.common.model;

import lombok.Data;

/**
 * 传输层消息封装
 */
@Data
public class RpcMessage {

    /**
     * 消息类型: 1 请求 2 响应 3 心跳 4 心跳响应
     */
    private byte type;

    /**
     * 请求 id,用于请求与响应的匹配
     */
    private int requestId;

    /**
     * 消息体: RpcRequest / RpcResponse / 心跳内容
     */
    private Object data;

}

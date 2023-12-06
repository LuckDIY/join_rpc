package com.example.join_rpc.common.serialize;

import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;

/**
 * 消息序列化接口
 */
public interface MessageSerialize {

    /**
     * 序列化请求
     */
    byte[] marshallingRequest(RpcRequest request) throws Exception;

    /**
     * 反序列化请求
     */
    RpcRequest unmarshallingRequest(byte[] data) throws Exception;

    /**
     * 序列化响应
     */
    byte[] marshallingResponse(RpcResponse response) throws Exception;

    /**
     * 反序列化响应
     */
    RpcResponse unmarshallingResponse(byte[] data) throws Exception;
}

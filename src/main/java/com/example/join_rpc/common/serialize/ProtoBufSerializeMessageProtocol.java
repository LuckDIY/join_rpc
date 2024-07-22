package com.example.join_rpc.common.serialize;


import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.example.join_rpc.utils.SerializingUtil;

/**
 * ProtoBuf序列化消息协议
 *
 * @Author: ppphuang
 * @Create: 2021/9/14
 */
public class ProtoBufSerializeMessageProtocol implements MessageSerialize {


    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {
        return SerializingUtil.serialize(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data, RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return SerializingUtil.serialize(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data, RpcResponse.class);
    }
}

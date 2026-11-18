package io.github.chaoleiwang.rpc.common.serialize;

import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;
import io.github.chaoleiwang.rpc.utils.SerializingUtil;

/**
 * 基于 protostuff 的序列化实现,序列化结果自带类型信息,无需额外类型转换。
 * <p>
 * 通过 rpc.serializer=protostuff 启用。
 */
public class ProtostuffMessageSerialize implements MessageSerialize {

    @Override
    public byte[] marshallingRequest(RpcRequest request) {
        return SerializingUtil.serialize(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) {
        return SerializingUtil.deserialize(data, RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) {
        return SerializingUtil.serialize(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) {
        return SerializingUtil.deserialize(data, RpcResponse.class);
    }

}

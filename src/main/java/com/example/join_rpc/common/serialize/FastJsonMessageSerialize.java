package com.example.join_rpc.common.serialize;

import com.alibaba.fastjson.JSONObject;
import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;

public class FastJsonMessageSerialize implements MessageSerialize{
    @Override
    public byte[] marshallingRequest(RpcRequest request) throws Exception {

        return JSONObject.toJSONBytes(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) throws Exception {

        return JSONObject.parseObject(new String(data), RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) throws Exception {
        return JSONObject.toJSONBytes(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) throws Exception {
        return JSONObject.parseObject(new String(data), RpcResponse.class);
    }
}

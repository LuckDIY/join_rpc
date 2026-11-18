package io.github.chaoleiwang.rpc.common.serialize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;

import java.lang.reflect.Type;

/**
 * 基于 fastjson2 的 JSON 序列化实现(默认)。
 * <p>
 * 传输内容不携带类名信息(不开启 autoType,避免反序列化漏洞),
 * 参数/返回值先以 JSON 树传输,到达对端后按方法声明的类型做转换。
 */
public class JsonMessageSerialize implements MessageSerialize {

    @Override
    public byte[] marshallingRequest(RpcRequest request) {
        return JSON.toJSONBytes(request);
    }

    @Override
    public RpcRequest unmarshallingRequest(byte[] data) {
        return JSON.parseObject(data, RpcRequest.class);
    }

    @Override
    public byte[] marshallingResponse(RpcResponse response) {
        return JSON.toJSONBytes(response);
    }

    @Override
    public RpcResponse unmarshallingResponse(byte[] data) {
        return JSON.parseObject(data, RpcResponse.class);
    }

    @Override
    public Object convertParameter(Object parameter, Class<?> targetType) {
        return convert(parameter, targetType);
    }

    @Override
    public Object convertReturnValue(Object returnValue, Type targetType) {
        return convert(returnValue, targetType);
    }

    /**
     * JSON 树 -> 目标类型转换
     */
    private Object convert(Object raw, Type targetType) {
        if (raw == null || targetType == void.class || targetType == Void.class) {
            return null;
        }
        if (raw instanceof JSONObject || raw instanceof JSONArray) {
            return JSON.parseObject(raw.toString(), targetType);
        }
        if (targetType instanceof Class<?> targetClass) {
            if (targetClass.isInstance(raw)) {
                return raw;
            }
            // 标量/字符串: 通过 JSON 文本转目标类型(支持枚举、日期、数值收窄等)
            return JSON.parseObject(JSON.toJSONString(raw), targetType);
        }
        // 泛型等复杂类型
        return JSON.parseObject(JSON.toJSONString(raw), targetType);
    }

}

package io.github.chaoleiwang.rpc.common.serialize;

import io.github.chaoleiwang.rpc.common.model.RpcRequest;
import io.github.chaoleiwang.rpc.common.model.RpcResponse;

import java.lang.reflect.Type;

/**
 * 消息序列化接口。
 * <p>
 * 序列化实现可通过配置 rpc.serializer 选择(内置 json / protostuff)。
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

    /**
     * 参数类型转换:对于 json 这类反序列化后不带类型信息的协议,
     * 服务端需要把参数树(JSONObject 等)转成方法声明的真实类型;protostuff 等带类型的协议直接用默认实现即可
     */
    default Object convertParameter(Object parameter, Class<?> targetType) {
        return parameter;
    }

    /**
     * 返回值类型转换,同 {@link #convertParameter(Object, Class)}
     */
    default Object convertReturnValue(Object returnValue, Type targetType) {
        return returnValue;
    }

}

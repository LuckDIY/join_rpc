package io.github.chaoleiwang.rpc.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * protostuff 序列化工具类
 */
public class SerializingUtil {

    /**
     * 将对象序列化为 byte 数组
     */
    public static <T> byte[] serialize(T source) {
        @SuppressWarnings("unchecked")
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(source.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtobufIOUtil.toByteArray(source, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 将 byte 数组反序列化为对象
     */
    public static <T> T deserialize(byte[] source, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T instance = schema.newMessage();
        ProtobufIOUtil.mergeFrom(source, instance, schema);
        return instance;
    }

}

package io.github.joinrpc.core.serialize;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 基于 protostuff runtime 的序列化，默认实现。
 * 要求被传输的类有无参构造器。
 * <p>
 * 注意：JDK 17 下 protostuff 无法处理 POJO 字段里的 java.util.Collections$* 包装集合
 * （unmodifiableList、singletonList、emptyList...），除非 JVM 加上 --add-opens java.base/java.util=ALL-UNNAMED。
 * 方法参数和返回值本身由 {@link io.github.joinrpc.core.protocol.JdkCollections} 自动转换，不受影响。
 */
public class ProtostuffSerializer implements Serializer {

    public static final byte ID = 1;

    static {
        // 保留数组/集合里的 null 元素，否则 null 会被丢掉，参数个数、List 内容都会对不上。
        // 必须在 protostuff RuntimeEnv 类初始化之前设置。
        if (System.getProperty("protostuff.runtime.preserve_null_elements") == null) {
            System.setProperty("protostuff.runtime.preserve_null_elements", "true");
        }
    }

    private static final ThreadLocal<LinkedBuffer> BUFFER =
            ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

    @Override
    public byte id() {
        return ID;
    }

    @Override
    public String name() {
        return "protostuff";
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Object obj) {
        Schema<Object> schema = (Schema<Object>) RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = BUFFER.get();
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        Schema<T> schema = RuntimeSchema.getSchema(type);
        T message = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, message, schema);
        return message;
    }
}

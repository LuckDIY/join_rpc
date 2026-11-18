package io.github.joinrpc.core.serialize;

/**
 * 序列化 SPI。自定义实现通过 META-INF/services/io.github.joinrpc.core.serialize.Serializer 注册。
 * <p>
 * {@link #id()} 会写进每一帧的协议头，服务端按请求帧里的 id 反序列化，并用同一种方式序列化响应，
 * 所以同一个服务端可以同时服务使用不同序列化方式的客户端。
 */
public interface Serializer {

    /**
     * @return 协议头中的序列化标识，全局唯一。1~31 保留给内置实现
     */
    byte id();

    /**
     * @return 配置中使用的名称，如 "protostuff"
     */
    String name();

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] data, Class<T> type);
}

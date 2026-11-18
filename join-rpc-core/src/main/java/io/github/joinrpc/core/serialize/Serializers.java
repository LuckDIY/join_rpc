package io.github.joinrpc.core.serialize;

import io.github.joinrpc.core.RpcException;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过 {@link ServiceLoader} 加载所有 {@link Serializer} 实现。
 */
public final class Serializers {

    private static final Map<Byte, Serializer> BY_ID = new ConcurrentHashMap<>();
    private static final Map<String, Serializer> BY_NAME = new ConcurrentHashMap<>();

    static {
        for (Serializer serializer : ServiceLoader.load(Serializer.class, Serializers.class.getClassLoader())) {
            register(serializer);
        }
    }

    private Serializers() {
    }

    /**
     * 手动注册一个序列化实现（不想用 SPI 文件时使用）。
     */
    public static void register(Serializer serializer) {
        Serializer existing = BY_ID.putIfAbsent(serializer.id(), serializer);
        if (existing != null && existing.getClass() != serializer.getClass()) {
            throw new IllegalStateException("duplicate serializer id " + serializer.id() + ": "
                    + existing.getClass().getName() + " and " + serializer.getClass().getName());
        }
        BY_NAME.putIfAbsent(serializer.name().toLowerCase(), serializer);
    }

    public static Serializer get(byte id) {
        Serializer serializer = BY_ID.get(id);
        if (serializer == null) {
            throw new RpcException("unknown serializer id: " + id);
        }
        return serializer;
    }

    public static Serializer get(String name) {
        Serializer serializer = BY_NAME.get(name.toLowerCase());
        if (serializer == null) {
            throw new RpcException("unknown serializer '" + name + "', available: " + BY_NAME.keySet());
        }
        return serializer;
    }
}

package io.github.joinrpc.core.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * 把 JDK 内部实现的集合（List.of、Collections.unmodifiableList、Arrays.asList、Stream.toList ...）
 * 转成普通的 ArrayList / HashSet / HashMap 等，再交给序列化框架。
 * <p>
 * 这些内部类要么是只读的（反序列化时无法 add），要么在 JDK 17 下不允许反射访问，很多序列化框架都处理不了。
 * 只转换方法参数/返回值本身以及嵌套在集合、Map、数组里的集合，不会深入 POJO 字段。
 */
public final class JdkCollections {

    private JdkCollections() {
    }

    @SuppressWarnings("unchecked")
    public static Object normalize(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> collection) {
            if (!isJdkInternal(value.getClass())) {
                return value;
            }
            Collection<Object> copy;
            if (collection instanceof SortedSet<?> sorted) {
                copy = new TreeSet<>((java.util.Comparator<Object>) sorted.comparator());
            } else if (collection instanceof Set<?>) {
                copy = new LinkedHashSet<>(Math.max(16, collection.size() * 2));
            } else {
                copy = new ArrayList<>(collection.size());
            }
            for (Object element : collection) {
                copy.add(normalize(element));
            }
            return copy;
        }
        if (value instanceof Map<?, ?> map) {
            if (!isJdkInternal(value.getClass())) {
                return value;
            }
            Map<Object, Object> copy = map instanceof SortedMap<?, ?> sorted
                    ? new TreeMap<>((java.util.Comparator<Object>) sorted.comparator())
                    : new LinkedHashMap<>(Math.max(16, map.size() * 2));
            map.forEach((k, v) -> copy.put(normalize(k), normalize(v)));
            return copy;
        }
        if (value instanceof Object[] array && !array.getClass().getComponentType().isPrimitive()) {
            for (int i = 0; i < array.length; i++) {
                array[i] = normalize(array[i]);
            }
        }
        return value;
    }

    /**
     * java.util 下的非 public 类（ImmutableCollections$ListN、Collections$UnmodifiableList、Arrays$ArrayList...）。
     * ArrayList、HashMap 等普通 public 类不需要转换。
     */
    private static boolean isJdkInternal(Class<?> type) {
        return type.getName().startsWith("java.util.") && !java.lang.reflect.Modifier.isPublic(type.getModifiers());
    }
}

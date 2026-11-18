package io.github.chaoleiwang.rpc.server.registry;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 本地服务对象:节点提供的具体服务
 */
@Data
@AllArgsConstructor
public class ServiceObject {

    /**
     * 服务名称,唯一(默认接口全限定名)
     */
    private String name;

    /**
     * 服务接口 Class
     */
    private Class<?> clazz;

    /**
     * 服务实现实例
     */
    private Object obj;

}

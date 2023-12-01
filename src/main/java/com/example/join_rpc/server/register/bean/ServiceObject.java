package com.example.join_rpc.server.register.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 节点服务对象，存储服务信息
 */
@Data
@AllArgsConstructor
public class ServiceObject {

    /**
     * 服务名称，唯一
     */
    private String name;

    /**
     * 服务Class
     */
    private Class<?> clazz;

    /**
     * 具体服务
     */
    private Object obj;

    /**
     * 服务分组
     */
    private String group;

    /**
     * 服务版本
     */
    private String version;

}

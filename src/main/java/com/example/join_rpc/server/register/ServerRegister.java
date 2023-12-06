package com.example.join_rpc.server.register;


import com.example.join_rpc.server.register.bean.ServiceObject;

/**
 * 节点 -> 服务注册接口，定义服务注册规范
 */
public interface ServerRegister {

    /**
     * 注册服务对象
     *
     * @param so 服务对象信息
     * @throws Exception
     */
    void register(ServiceObject so) throws Exception;

    /**
     * 移除服务对象
     *
     * @throws Exception
     */
    void remove();

    /**
     * 获取服务对象
     *
     * @param name
     * @return
     * @throws Exception
     */
    ServiceObject getServiceObject(String name);
}

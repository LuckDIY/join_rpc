package com.example.reg_center.server.register;

import com.example.reg_center.server.register.bean.ServiceObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务注册抽象类，抽象公共部分,提供模版方法
 */
public abstract class DefaultServerRegister implements ServerRegister{

    /**
     * 节点提供的服务列表
     */
    private Map<String, ServiceObject> serviceMap = new HashMap<>();


    @Override
    public void register(ServiceObject so) throws Exception {
        if (so == null) {
            throw new IllegalArgumentException("so:ServiceObject cannot be empty");
        }
        //服务名字+服务分组+版本
        serviceMap.put(so.getName() + so.getGroup() + so.getVersion(), so);
    }


    @Override
    public ServiceObject getServiceObject(String name) throws Exception {
        return serviceMap.get(name);
    }
}
